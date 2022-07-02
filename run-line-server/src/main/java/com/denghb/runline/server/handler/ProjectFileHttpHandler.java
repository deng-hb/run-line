package com.denghb.runline.server.handler;

import com.denghb.runline.server.Consts;
import com.denghb.runline.server.RunLineServer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectFileHttpHandler {

    // http://localhost:9966/project/km/src/main/java/com/denghb/km/http/StatHttpHandler.java
    public JSONObject handle(String path) {

        String filePath = path.replace("/project", String.format("%s/runline", RunLineServer.WORKSPACE));
        String projectName = path.split("/")[2];
        String projectPath = String.format("%s/runline/%s", RunLineServer.WORKSPACE, projectName);

        JSONObject jsonObject = new JSONObject();
        try {
            Git git = Git.open(new File(projectPath));
            String branch = git.getRepository().getBranch();
            String sourceFile = path.substring(path.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length());
            String runlinePath = String.format("%s/.runline/%s/%s/%s", RunLineServer.WORKSPACE, projectName, branch, sourceFile.replace(".java", ""));

            jsonObject.put("content", readContent(filePath));
            jsonObject.put("runline", runline(runlinePath));
            jsonObject.put("diff", gitDiff(git, branch, sourceFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

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

    private JSONArray runline(String filePath) {
        Set<String> lines = new HashSet<>();
        try (FileReader fileReader = new FileReader(filePath);
             LineNumberReader reader = new LineNumberReader(fileReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 2022-07-02 00:28:41.602#lambda$main$0:20
                String lineNumber = line.substring(line.lastIndexOf(":") + 1);
                lines.add(lineNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray(lines);
    }

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
