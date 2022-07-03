package com.denghb.runline.server.handler;

import com.denghb.runline.server.Consts;
import com.denghb.runline.server.RegistryHub;
import com.denghb.runline.server.RunLineServer;
import com.sun.net.httpserver.HttpExchange;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectHttpHandler extends BaseHttpHandler {


    @Override
    public void handle(HttpExchange httpExchange) {
        String path = getPath(httpExchange);
        Object res = handle(path);
        outJson(httpExchange, res);
    }

    public Object handle(String path) {
        Object res = "";
        if (path.startsWith("/projects")) {
            res = projects();// http://localhost:9966/projects
        } else if (path.startsWith("/project/") && path.endsWith(".java")) {
            res = projectFile(path);
        } else if (path.startsWith("/project/")) {
            res = projectPath(path);// http://localhost:9966/project/run-line
        }
        return res;
    }

    // 已经clone下来的项目
    public JSONArray projects() {
        JSONArray jsonArray = new JSONArray();
        File file = new File(RunLineServer.WORKSPACE);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return jsonArray;
            }
            for (File f : files) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", f.getName());

                jsonObject.put("git", getRemoteUrl(f));
                jsonArray.put(jsonObject);
            }
        }

        return jsonArray;
    }

    private String getRemoteUrl(File file) {
        try {
            Git git = Git.open(file);
            List<RemoteConfig> call = git.remoteList().call();
            return call.get(0).getURIs().get(0).toString();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 项目目录
    public JSONObject projectPath(String path) {
        JSONObject jsonObject = new JSONObject();
        String projectPath = path.replace("/project", RunLineServer.WORKSPACE);
        String projectName = projectPath.substring(projectPath.lastIndexOf("/") + 1);

        File file = new File(projectPath);
        JSONObject subJsonObject = readFiles(file.listFiles());
        jsonObject.put(projectName, subJsonObject);
        return jsonObject;
    }

    // 文件目录
    private JSONObject readFiles(File[] files) {
        JSONObject jsonObject = new JSONObject();
        if (null == files) {
            return jsonObject;
        }
        for (File file : files) {
            String absolutePath = file.getAbsolutePath().replace(RunLineServer.WORKSPACE, "");
            String fileName = file.getName();
            if (fileName.startsWith(".")) {
                continue;// .git
            }
            if (file.isDirectory()) {
                JSONObject subJsonObject = readFiles(file.listFiles());
                jsonObject.put(fileName, subJsonObject);
            } else {
                jsonObject.put(fileName, absolutePath);
            }
        }
        return jsonObject;
    }

    //  http://localhost:9966/project/run-line/run-line-server/src/main/java/com/denghb/runline/server/handler/GitHandler.java
    public JSONObject projectFile(String path) {

        String filePath = path.replace("/project", RunLineServer.WORKSPACE);
        String projectName = path.split("/")[2];
        String projectPath = String.format("%s/%s", RunLineServer.WORKSPACE, projectName);

        JSONObject jsonObject = new JSONObject();
        try {
            Git git = Git.open(new File(projectPath));
            String branch = git.getRepository().getBranch();
            String sourceFile = path.substring(path.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length());

            String runlinePath = String.format("runline/%s/%s/%s", projectName, branch, sourceFile.replace(".java", ""));
            List<String> clients = RegistryHub.getOnline(projectName, branch);

            jsonObject.put("content", readContent(filePath));
            jsonObject.put("runline", runline(clients, runlinePath));
            jsonObject.put("diff", gitDiff(git, branch, sourceFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
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
    private JSONArray runline(List<String> clients, String filePath) {
        Set<String> lines = new HashSet<>();
        for (String client : clients) {
            try {
                String url = String.format("http://%s/%s", client, filePath);
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
}
