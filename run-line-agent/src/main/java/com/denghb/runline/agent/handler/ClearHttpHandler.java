package com.denghb.runline.agent.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 清理
 */
public class ClearHttpHandler implements HttpHandler {

    private final String workspace;

    public ClearHttpHandler(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // /runline/run-line/master/com/denghb/App

        try (OutputStream out = httpExchange.getResponseBody()) {

            String path = httpExchange.getRequestURI().getPath();
            if (path.contains("/..")) {
                throw new IllegalArgumentException();
            }
            // 清理
            String runlinePath = path.replaceFirst("/api/runline/clear", workspace);
            File file = new File(runlinePath);
            if (file.isDirectory()) {
                delFiles(file.listFiles());
            }
            file.delete();
            httpExchange.sendResponseHeaders(200, 0);
            out.write("ok".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

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
}
