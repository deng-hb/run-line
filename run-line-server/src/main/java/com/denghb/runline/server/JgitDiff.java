package com.denghb.runline.server;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JgitDiff {



    static final Map<String, EditList> DATA = new HashMap<>();

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

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());

        for (DiffEntry diffEntry : diffs) {
            FileHeader fileHeader = df.toFileHeader(diffEntry);
            EditList editList = fileHeader.toEditList();
            String newPath = diffEntry.getNewPath();
            System.out.println("NewPath:\t" + newPath);
            System.out.println("OldPath:\t" + diffEntry.getOldPath());
            int i = newPath.indexOf(Consts.SOURCE_FOLDER);
            if (i != -1) {
                String source = newPath.substring(i + Consts.SOURCE_FOLDER.length());
                DATA.put(source, editList);
                System.out.println(source);
                for (Edit edit : editList) {
                    System.out.println(edit.getBeginA());
                    System.out.println(edit.getEndA());
                    System.out.println("--------");
                    System.out.println(edit.getBeginB());
                    System.out.println(edit.getEndB());
                    System.out.println(edit.getType());
                }
            }
            //  out.reset();
        }

        System.out.println(DATA);

    }

}