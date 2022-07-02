package com.denghb.runline.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RunLineData {
    public final static String WORKSPACE = "/Users/mac/.runline/example/dev";

    public static Map<String, Set<String>> DATA = new HashMap<>();

    public static void main(String[] args) {

        read(new File(WORKSPACE));

    }

    public static void read(File file) {
        if (file.isDirectory()) {
            reads(file.listFiles());
        } else {
            String absolutePath = file.getAbsolutePath();
            System.out.println(absolutePath);
            String className = absolutePath.substring(WORKSPACE.length() + 1);
            System.out.println(className);
            Set<String> lines = DATA.computeIfAbsent(className, k -> new HashSet<>());

            try (FileReader fileReader = new FileReader(file);
                 LineNumberReader reader = new LineNumberReader(fileReader)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 2022-07-02 00:28:41.602#lambda$main$0:20
                    String lineNumber = line.substring(line.lastIndexOf(":") + 1);
                    lines.add(lineNumber);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void reads(File[] files) {
        for (File file : files) {
            read(file);
        }

    }
}
