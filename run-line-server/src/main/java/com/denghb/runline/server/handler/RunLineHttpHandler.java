package com.denghb.runline.server.handler;

import com.denghb.runline.server.Consts;
import com.denghb.runline.server.GitUtil;
import com.denghb.runline.server.RegistryHub;
import com.denghb.runline.server.RunLineServer;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
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

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/**
 * /runline/${project} -- 列出所有比master有变更的java
 * /runline/${project}/${filepath}.java -- 具体对比的行，和运行情况
 */
@Slf4j
public class RunLineHttpHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);
        String filePath = path.replaceFirst("/runline", RunLineServer.WORKSPACE);
        String project = path.split("/")[2];

        Object res = "ok";
        try (Git git = Git.open(GitUtil.getExistProject(project))) {
            String branch = git.getRepository().getBranch();
            if (path.endsWith(".java")) {
                JSONObject jsonObject = new JSONObject();
                List<String> content = readContent(filePath);
                jsonObject.put("content", content);

                String sourceFile = path.substring(path.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length());
                jsonObject.put("gitdiff", gitDiff(git, branch, sourceFile));

                List<List<String>> runline = RegistryHub.getRunline(project, branch, sourceFile.replace(".java", ""));
                if (runline.size() == 2) {
                    jsonObject.put("allline", runline.get(0));
                    jsonObject.put("runline", runline.get(1));
                }

                res = jsonObject;

            } else {
                JSONArray jsonArray = gitDiff(git, branch, null);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String file = jsonObject.getString("file");

                    String sourceFile = file.substring(file.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length());
                    List<List<String>> runline = RegistryHub.getRunline(project, branch, sourceFile.replace(".java", ""));
                    if (runline.size() == 2) {
                        Set<String> allLine = new HashSet<>(runline.get(0));
                        double allLineSize = allLine.size() * 1.;

                        double runLineSize = 0.;
                        List<String> runLine = runline.get(1);
                        for (String s : runLine) {
                            String[] split = s.split(";");
                            if (allLine.contains(split[0])) {
                                runLineSize++;
                            }
                        }
                        double runLineRate = runLineSize / allLineSize * 100.;

                        JSONArray diffLine = gitDiff(git, branch, sourceFile);
                        double diffLineSize = 0.;
                        for (int j = 0; j < diffLine.length(); j++) {
                            JSONObject jsonObject1 = diffLine.getJSONObject(j);
                            if (jsonObject1.has("insert")) {
                                JSONArray insert = jsonObject1.getJSONArray("insert");
                                int start = insert.getInt(0), end = insert.getInt(1);
                                for (; start <= end; start++) {
                                    if (allLine.contains(String.valueOf(start))) {
                                        diffLineSize++;
                                    }
                                }
                            }
                            if (jsonObject1.has("replace")) {
                                JSONArray replace = jsonObject1.getJSONArray("replace");
                                int start = replace.getInt(0), end = replace.getInt(1);
                                for (; start <= end; start++) {
                                    if (allLine.contains(String.valueOf(start))) {
                                        diffLineSize++;
                                    }
                                }
                            }
                        }
                        double diffLineRate = diffLineSize / allLineSize * 100.;
                        jsonObject.put("runline", new HashMap<String, String>() {{
                            put("diff", String.format("%.2f%%", diffLineRate));// 变更覆盖
                            put("line", String.format("%.2f%%", runLineRate));// 行覆盖
                        }});
                    }

                }

                res = jsonArray;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res = e.getMessage();
        }

        outJson(httpExchange, res);

    }

    public static List<String> readContent(String filePath) {

        List<String> list = new ArrayList<>();
        try {
            LineNumberReader numberReader = null;
            numberReader = new LineNumberReader(new FileReader(filePath));
            String code;
            while (null != (code = numberReader.readLine())) {
                int lineNumber = numberReader.getLineNumber();
                list.add(code);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return list;
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
                    jsonObject.put("replace", new JSONArray() {{
                        put(edit.getBeginB());
                        put(edit.getEndB());
                    }});
                    jsonArray.put(jsonObject);
                } else if (edit.getType() == Edit.Type.INSERT) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("insert", new JSONArray() {{
                        put(edit.getBeginB());
                        put(edit.getEndB());
                    }});
                    jsonArray.put(jsonObject);
                }
            }
        }

        return jsonArray;
    }

}
