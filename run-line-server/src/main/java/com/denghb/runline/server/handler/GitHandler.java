package com.denghb.runline.server.handler;

import com.denghb.runline.server.RunLineServer;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class GitHandler {


    public Object handle(String path) {
        if (path.startsWith("/git/clone/")) {
            // http://localhost:9966/git/clone/https://github.com/deng-hb/run-line.git
            // http://localhost:9966/git/clone/git@github.com:deng-hb/run-line.git
            String url = path.substring("/git/clone/".length());
            Object projectName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
            String dir = String.format("%s/runline/%s", RunLineServer.WORKSPACE, projectName);

            System.out.println("git clone " + url);
            System.out.println("git dir " + dir);

            try {

                Git git = Git.cloneRepository()
                        .setURI(url)
                        .setBare(false)
                        .setDirectory(new File(dir))
                        .setCloneAllBranches(true)
                        .call();
                git.getRepository().close();

            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        return "ok";
    }

}
