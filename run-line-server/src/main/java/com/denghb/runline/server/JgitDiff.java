package com.denghb.runline.server;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class JgitDiff {

    public static void main(String[] args) throws Exception {

        diffMaster("dev");

    }

    public static void diffMaster(String branch) throws Exception {
        Git git = Git.open(new File("/Users/mac/IntelliJIDEAProjects/run-line/.git"));
        Repository repository = git.getRepository();

        ObjectId masterObjectId = repository.resolve("refs/heads/master^{tree}");
        ObjectId branchObjectId = repository.resolve((String.format("refs/heads/%s^{tree}", branch)));

        if (null == branchObjectId) {
            throw new Exception(String.format("branch: %s not found", branch));
        }

        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser masterTree = new CanonicalTreeParser();
        masterTree.reset(reader, masterObjectId);

        CanonicalTreeParser branchTree = new CanonicalTreeParser();
        branchTree.reset(reader, branchObjectId);


        List<DiffEntry> diffs = git.diff()
                .setNewTree(branchTree)
                .setOldTree(masterTree)
                .call();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(git.getRepository());

        for (DiffEntry diffEntry : diffs) {
            df.format(diffEntry);
            System.out.println("NewPath:\t" + diffEntry.getNewPath());
            System.out.println("OldPath:\t" + diffEntry.getOldPath());
            String diffText = out.toString("UTF-8");
            System.out.println(diffText);
            //  out.reset();
        }


    }

}