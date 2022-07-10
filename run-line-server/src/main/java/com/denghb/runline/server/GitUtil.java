package com.denghb.runline.server;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class GitUtil {


    public static void checkout(String project, String branch) throws Exception {
        File file = getExistProject(project);

        try (Git git = Git.open(file)) {
            git.checkout()
                    .setCreateBranch(true)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setName(branch)
                    .setStartPoint("origin/" + branch)
                    .call();
        }
    }

    public static void clone(String project, String url) throws Exception {
        File file = getNotExistProject(project);
        try (Git git = Git.cloneRepository()
                .setURI(url)
                .setBare(false)
                .setDirectory(file)
                .setCloneAllBranches(true)
                .setCloneSubmodules(true)
                .call()) {

        }
    }

    public static void fetch(String project) throws Exception {
        File file = getExistProject(project);
        try (Git git = Git.open(file)) {
            git.fetch().call();
        }

    }

    public static void pull(String project) throws Exception {
        File file = getExistProject(project);
        try (Git git = Git.open(file)) {
            git.pull().call();
        }
    }

    public static void del(String project) {
        File file = getExistProject(project);
        if (file.isDirectory()) {
            delFiles(file.listFiles());
        }
        file.delete();
    }

    private static void delFiles(File[] files) {
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                delFiles(file.listFiles());
            }
            file.delete();
        }
    }

    public static String getProjectPath(String project) {

        String projectPath = String.format("%s/%s", RunLineServer.WORKSPACE, project);
        if (projectPath.contains("/..")) {
            throw new IllegalArgumentException(String.format("[%s] fail", project));
        }
        return projectPath;
    }

    public static File getExistProject(String project) {

        String projectPath = getProjectPath(project);
        File file = new File(projectPath);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("project [%s] not exist", project));
        }
        return file;
    }

    public static File getNotExistProject(String project) {
        String projectPath = getProjectPath(project);
        File file = new File(projectPath);
        if (file.exists()) {
            throw new IllegalArgumentException(String.format("project [%s] exist", project));
        }
        return file;
    }

}
