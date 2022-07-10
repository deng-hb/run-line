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
        String path = httpExchange.getRequestURI().getPath();
        try (OutputStream out = httpExchange.getResponseBody()) {
            if (path.contains("/..")) {
                throw new IllegalArgumentException();
            }
            // 清理
            String runlinePath = path.replaceFirst("/api/runline/clear", workspace);
            System.out.printf("clear:%s%n", runlinePath);
            delFiles(new File(runlinePath).listFiles());

            httpExchange.sendResponseHeaders(200, 0);
            out.write("ok".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println(path);
            e.printStackTrace();
        }

    }

    private static void delFiles(File[] files) {
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                delFiles(file.listFiles((dir, name) -> !".java".equals(name)));
            }
            file.delete();
        }
    }
}
