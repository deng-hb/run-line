package com.denghb.runline.server.handler;

import com.denghb.runline.server.RegistryHub;
import com.denghb.runline.server.RunLineServer;
import com.denghb.runline.server.tools.SourceTools;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RemoteConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        } else {
            res = projectPath(path);// http://localhost:9966/project/run-line
        }
        return res;
    }

    // 已经clone下来的项目
    public JSONArray projects() {
        JSONArray jsonArray = new JSONArray();
        File projects = new File(RunLineServer.WORKSPACE);
        if (projects.exists() && projects.isDirectory()) {
            File[] files = projects.listFiles();
            if (null == files) {
                return jsonArray;
            }
            for (File file : files) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", file.getName());
                JSONObject gitInfo = gitInfo(file);
                jsonObject.put("git", gitInfo);

                String branch = gitInfo.has("branch") ? gitInfo.getString("branch") : "";
                jsonObject.put("agents", RegistryHub.getOnline(file.getName(), branch));
                jsonArray.put(jsonObject);
            }
        }

        return jsonArray;
    }

    private JSONObject gitInfo(File file) {
        JSONObject jsonObject = new JSONObject();
        try {
            Git git = Git.open(file);
            jsonObject.put("branch", git.getRepository().getBranch());

            List<String> branchList = git.branchList().call().stream().map(Ref::getName).collect(Collectors.toList());
            jsonObject.put("branchList", branchList);

            List<RemoteConfig> remotes = git.remoteList().call();// 默认算1个吧
            jsonObject.put("remote", remotes.get(0).getURIs().get(0).toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return jsonObject;
    }

    // 项目目录
    public JSONObject projectPath(String path) {
        JSONObject jsonObject = new JSONObject();
        String filePath = path.replace("/project", RunLineServer.WORKSPACE);

        File file = new File(filePath);
        if (file.isDirectory()) {
            String projectName = filePath.substring(filePath.lastIndexOf("/") + 1);
            JSONObject subJsonObject = readFiles(file.listFiles());
            jsonObject.put(projectName, subJsonObject);
        } else {
            jsonObject.put("content", SourceTools.readCodes(filePath));
        }
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

}
