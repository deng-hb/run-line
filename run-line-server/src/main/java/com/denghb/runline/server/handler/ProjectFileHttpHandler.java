package com.denghb.runline.server.handler;

import com.denghb.runline.server.Consts;
import com.denghb.runline.server.RunLineServer;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.Set;

public class ProjectFileHttpHandler {

    // http://localhost:9966/project/km/src/main/java/com/denghb/km/http/StatHttpHandler.java
    public JSONObject handle(String path) throws IOException {
        JSONObject jsonObject = new JSONObject();
        String filePath = path.replace("/project", String.format("%s/runline", RunLineServer.WORKSPACE));

        String projectName = path.split("/")[2];
        String projectPath = String.format("%s/runline/%s", RunLineServer.WORKSPACE, projectName);
        Git git = Git.open(new File(projectPath));
        String branch = git.getRepository().getBranch();

        String className = path.substring(path.indexOf(Consts.SOURCE_FOLDER) + Consts.SOURCE_FOLDER.length()).replace(".java", "");
        String runlinePath = String.format("%s/.runline/%s/%s/%s", RunLineServer.WORKSPACE, projectName, branch, className);

        jsonObject.put("content", readContent(filePath));
        jsonObject.put("diff", gitDiff(filePath));
        jsonObject.put("runline", runline(runlinePath));
        return jsonObject;
    }

    private JSONArray gitDiff(String filePath) {
        JSONArray jsonArray = new JSONArray();
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
