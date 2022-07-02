package com.denghb.runline.server.handler;

import com.denghb.runline.server.RunLineServer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProjectsHttpHandler {

    public JSONArray handle() {
        JSONArray jsonArray = new JSONArray();
        File file = new File(String.format("%s/runline", RunLineServer.WORKSPACE));
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
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
}
