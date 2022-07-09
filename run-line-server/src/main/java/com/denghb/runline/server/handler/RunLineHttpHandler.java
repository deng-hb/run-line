package com.denghb.runline.server.handler;

import com.denghb.runline.server.Consts;
import com.denghb.runline.server.RegistryHub;
import com.denghb.runline.server.RunLineServer;
import com.sun.net.httpserver.HttpExchange;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.attributes.Attribute;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * /runline/${project} -- 列出所有比master有变更的java
 * /runline/${project}/${filepath}.java -- 具体对比的行，和运行情况
 */
public class RunLineHttpHandler extends BaseHttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);
        String filePath = path.replaceFirst("/runline", RunLineServer.WORKSPACE);
        String projectName = path.split("/")[2];
        String projectPath = String.format("%s/%s", RunLineServer.WORKSPACE, projectName);
        JSONObject jsonObject = new JSONObject();
        Git git = Git.open(new File(projectPath));
        String branch = git.getRepository().getBranch();

        if (path.endsWith(".java")) {
            jsonObject.put("content", readContent(filePath));
            String sourceFile = path.substring(path.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length());
            try {
                jsonObject.put("diff", gitDiff(git, branch, sourceFile));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String api = String.format("/api/runline/%s/%s/%s", projectName, branch, sourceFile.replace(".java", ""));
            List<String> clients = RegistryHub.getOnline(projectName, branch);
            jsonObject.put("runline", runline(clients, api));
            outJson(httpExchange, jsonObject);

        } else {
            try {
                JSONArray jsonArray = gitDiff(git, branch, null);
                outJson(httpExchange, jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    // 文件内容
    private JSONArray readContent(String filePath) {
        JSONArray jsonArray = new JSONArray();
        try (FileReader fileReader = new FileReader(filePath);
             LineNumberReader reader = new LineNumberReader(fileReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonArray.put(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    // 和master分支比较
    private JSONArray gitDiff(Git git, String branch, String sourceFile) throws Exception {
        Repository repository = git.getRepository();

        ObjectId masterObjectId = repository.resolve("refs/heads/master^{tree}");
        ObjectId branchObjectId = repository.resolve((String.format("refs/heads/%s^{tree}", branch)));


        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser masterTree = new CanonicalTreeParser();
        masterTree.reset(reader, masterObjectId);

        CanonicalTreeParser branchTree = new CanonicalTreeParser();
        branchTree.reset(reader, branchObjectId);


        List<DiffEntry> diffs = git.diff()
                .setNewTree(branchTree)
                .setOldTree(masterTree)
                .call();

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());

        JSONArray jsonArray = new JSONArray();

        for (DiffEntry diffEntry : diffs) {
            String newPath = diffEntry.getNewPath();
            Attribute diffAttribute = diffEntry.getDiffAttribute();
            System.out.println(diffAttribute);
            // all
            if (null == sourceFile) {
                if (newPath.contains(Consts.SOURCE_FOLDER) && newPath.endsWith(".java")) {

                    Iterable<RevCommit> commits = git.log().addPath(newPath).call();
                    JSONObject jsonObject = new JSONObject();
                    RevCommit commit = commits.iterator().next();
                    jsonObject.put("file", newPath);
                    jsonObject.put("commit", new HashMap<String, String>() {{
                        put("author", commit.getAuthorIdent().getName());
                        put("time", Consts.SDF.get().format(new Date(commit.getCommitTime() * 1000L)));
                        put("message", commit.getShortMessage());
                    }});
                    jsonArray.put(jsonObject);
                }
                continue;
            }
            // single
            if (!newPath.endsWith(sourceFile)) {
                continue;
            }

            FileHeader fileHeader = df.toFileHeader(diffEntry);
            EditList editList = fileHeader.toEditList();
            for (Edit edit : editList) {
                if (edit.getType() == Edit.Type.REPLACE) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("replace", new int[]{
                            edit.getBeginB(), edit.getEndB()
                    });
                    jsonArray.put(jsonObject);
                } else if (edit.getType() == Edit.Type.INSERT) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("insert", new int[]{
                            edit.getBeginB(), edit.getEndB()
                    });
                    jsonArray.put(jsonObject);
                }
            }
        }

        return jsonArray;
    }

    //  运行过的行数
    private JSONArray runline(List<String> clients, String api) {
        Set<String> lines = new HashSet<>();
        for (String client : clients) {
            try {
                String url = String.format("http://%s%s", client, api);
                URLConnection connection = new URL(url).openConnection();
                try (InputStream in = connection.getInputStream()) {

                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }
                    String res = output.toString();
                    if (null != res && res.length() > 0) {
                        lines.addAll(Arrays.asList(res.split(",")));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new JSONArray(lines);
    }
}
