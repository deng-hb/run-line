package com.denghb.runline.server.handler;

import com.denghb.runline.server.RunLineServer;
import com.sun.net.httpserver.HttpExchange;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

public class GitOperateHttpHandler extends BaseHttpHandler {


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);
        Object res = handle(path);
        outJson(httpExchange, res);
    }

    public Object handle(String path) {
        try {
            String[] split = path.split("/");
            String command = split[2];
            String projectName = split[3];
            String projectPath = String.format("%s/runline/%s", RunLineServer.WORKSPACE, projectName);

            switch (command) {
                case "clone":// http://localhost:9966/git/clone/git@github.com:deng-hb/run-line.git
                    gitClone(path);
                    break;
                case "checkout":// http://localhost:9966/git/checkout/run-line/dev
                    String branchName = split[4];
                    gitCheckout(projectPath, branchName);
                    break;
                case "fetch":// http://localhost:9966/git/fetch/run-line
                    Git.open(new File(projectPath)).fetch().call();
                    break;
                case "pull":// http://localhost:9966/git/pull/run-line
                    Git.open(new File(projectPath)).pull().call();
                    break;

            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "ok";
    }


    private void gitCheckout(String projectPath, String branchName) throws Exception {

        Git.open(new File(projectPath))
                .checkout()
                .setCreateBranch(true)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setName(branchName)
                .setForce(true)
                .setStartPoint("origin/" + branchName)
                .call();
    }


    private void gitClone(String path) throws Exception {
        String url = path.substring("/git/clone/".length());
        Object projectName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        String projectPath = String.format("%s/runline/%s", RunLineServer.WORKSPACE, projectName);

        Git git = Git.cloneRepository()
                .setURI(url)
                .setBare(false)
                .setDirectory(new File(projectPath))
                .setCloneAllBranches(true)
                .setCloneSubmodules(true)
                .call();
        git.getRepository().close();
    }

}
