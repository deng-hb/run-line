package com.denghb.runline.agent;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class RunLineHttpHandler implements HttpHandler {

    private String workspace;

    public RunLineHttpHandler(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // /runline/run-line/master/com/denghb/App

        try (OutputStream out = httpExchange.getResponseBody()) {

            String path = httpExchange.getRequestURI().getPath();
            String runlinePath = path.replaceFirst("/runline", workspace);

            StringBuilder lines = new StringBuilder();
            File[] files = new File(runlinePath).listFiles();
            readFiles(files, lines);

            httpExchange.sendResponseHeaders(200, 0);
            out.write(lines.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readFiles(File[] files, StringBuilder lines) {
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                readFiles(file.listFiles(), lines);
            } else {
                if (lines.length() > 0) {
                    lines.append(",");
                }
                lines.append(file.getName());
                lines.append(":");
                lines.append(createTime(file));
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
