package com.denghb.runline.server.handler;


import com.denghb.runline.server.RunLineServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class ProjectPathHttpHandler {

    public JSONObject handle(String path) {
        JSONObject jsonObject = new JSONObject();
        String projectPath = path.replace("/project", String.format("%s/runline", RunLineServer.WORKSPACE));
        String projectName = projectPath.substring(projectPath.lastIndexOf("/") + 1);

        File file = new File(projectPath);
        JSONObject subJsonObject = readFiles(file.listFiles());
        jsonObject.put(projectName, subJsonObject);
        return jsonObject;
    }

    private JSONObject readFiles(File[] files) {
        JSONObject jsonObject = new JSONObject();
        for (File file : files) {
            String absolutePath = file.getAbsolutePath().replace(String.format("%s/runline", RunLineServer.WORKSPACE), "");
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

}
