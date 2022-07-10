package com.denghb.runline.agent.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 返回两行
 * 第一行源代码行号,行号
 * 第二行运行过的行号:时间戳,行号:时间戳
 */
public class StatHttpHandler implements HttpHandler {

    private final String workspace;

    public StatHttpHandler(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // /runline/stat/run-line/master/com/denghb/App

        try (OutputStream out = httpExchange.getResponseBody()) {

            String path = httpExchange.getRequestURI().getPath();
            if (path.contains("/..")) {
                throw new IllegalArgumentException();
            }
            String runlinePath = path.replaceFirst("/api/runline/stat", workspace);

            StringBuilder allline = new StringBuilder();
            File[] files = new File(String.format("%s/.java", runlinePath)).listFiles();
            readFiles(files, allline, false);

            StringBuilder runline = new StringBuilder();
            files = new File(runlinePath).listFiles((dir, name) -> !".java".equals(name));
            readFiles(files, runline, true);

            httpExchange.sendResponseHeaders(200, 0);
            out.write(String.format("%s\n%s", allline, runline).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readFiles(File[] files, StringBuilder sb, boolean addTime) {
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                readFiles(file.listFiles(), sb, addTime);
            } else {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(file.getName());
                if (addTime) {
                    sb.append(":");
                    sb.append(createTime(file));
                }
            }
        }
    }

    private static long createTime(File file) {
        try {
            Path path = file.toPath();
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            return attr.creationTime().toMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
