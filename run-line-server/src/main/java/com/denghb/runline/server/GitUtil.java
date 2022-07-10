package com.denghb.runline.server;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.util.List;

public class GitUtil {


    public static void checkout(String project, String branch) throws Exception {
        File file = getExistProject(project);
        Git git = Git.open(file);

        git.checkout()
                .setCreateBranch(true)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setName(branch)
                .setStartPoint("origin/" + branch)
                .call();
        git.getRepository().close();
        git.close();
    }

    public static List<DiffEntry> diff(String project, String branch) throws Exception {
        File file = getExistProject(project);
        Git git = Git.open(file);
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
        return diffs;
    }

    public static void clone(String project, String branch, String url) throws Exception {
        File file = getNotExistProject(project);
        Git git = Git.cloneRepository()
                .setURI(url)
                .setBare(false)
                .setBranch(branch)
                .setDirectory(file)
                .setCloneAllBranches(true)
                .setCloneSubmodules(true)
                .call();
        git.getRepository().close();
        git.close();
    }

    public static void fetch(String project) throws Exception {
        File file = getExistProject(project);
        Git git = Git.open(file);
        git.fetch().call();
        git.getRepository().close();
        git.close();
    }

    public static void pull(String project) throws Exception {
        File file = getExistProject(project);

        Git git = Git.open(file);
        git.pull().call();
        git.getRepository().close();
        git.close();
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
