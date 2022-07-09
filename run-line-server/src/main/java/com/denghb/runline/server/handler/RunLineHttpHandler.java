package com.denghb.runline.server.handler;

import com.denghb.runline.server.Consts;
import com.denghb.runline.server.RegistryHub;
import com.denghb.runline.server.RunLineServer;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * /runline/${project} -- 列出所有比master有变更的java
 * /runline/${project}/${filepath}.java -- 具体对比的行，和运行情况
 */
@Slf4j
public class RunLineHttpHandler extends BaseHttpHandler {

    public Object handle(String path) throws Exception {
        String filePath = path.replaceFirst("/runline", RunLineServer.WORKSPACE);
        String projectName = path.split("/")[2];
        String projectPath = String.format("%s/%s", RunLineServer.WORKSPACE, projectName);

        Git git = Git.open(new File(projectPath));
        String branch = git.getRepository().getBranch();

        if (path.endsWith(".java")) {
            JSONObject jsonObject = new JSONObject();
            List<String> content = readContent(filePath);
            jsonObject.put("content", content);

            String sourceFile = path.substring(path.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length());
            jsonObject.put("gitdiff", gitDiff(git, branch, sourceFile));

            List<List<String>> res = RegistryHub.getRunline(projectName, branch, sourceFile.replace(".java", ""));
            if (res.size() == 2) {
                jsonObject.put("allline", res.get(0));
                jsonObject.put("runline", res.get(1));
            }

            return jsonObject;

        } else {
            return gitDiff(git, branch, null);
        }


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

}
