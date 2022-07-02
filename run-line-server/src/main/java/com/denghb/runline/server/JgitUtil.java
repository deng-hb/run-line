package com.denghb.runline.server;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

public class JgitUtil {

    public static String getBranch(String projectPath) {

        try {
            Git git = Git.open(new File(projectPath));
            return git.getRepository().getBranch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
