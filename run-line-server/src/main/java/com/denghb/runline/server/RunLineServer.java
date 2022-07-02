package com.denghb.runline.server;


import com.denghb.runline.server.handler.GitHandler;
import com.denghb.runline.server.handler.ProjectFileHttpHandler;
import com.denghb.runline.server.handler.ProjectPathHttpHandler;
import com.denghb.runline.server.handler.ProjectsHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class RunLineServer {

    public static String WORKSPACE;

    public static void main(String[] args) throws IOException {
        System.out.println("args:" + Arrays.toString(args));
        if (args.length != 1 || args[0].endsWith("/")) {
            throw new IllegalArgumentException("java -jar run-line-server.jar ${workspace}");
        }
        WORKSPACE = String.format("%s", args[0]);

        HttpServer server = HttpServer.create(new InetSocketAddress(9966), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", (httpExchange) -> {
            String path = httpExchange.getRequestURI().getPath();
            System.out.printf("http:%s\n", path);

            Object res = null;
            if (path.startsWith("/git/")) {
                res = new GitHandler().handle(path);
            } else if (path.equals("/projects")) {
                res = new ProjectsHttpHandler().handle();
            } else if (path.startsWith("/project/") && path.endsWith(".java")) {
                res = new ProjectFileHttpHandler().handle(path);
            } else if (path.startsWith("/project/")) {
                res = new ProjectPathHttpHandler().handle(path);
            }

            if (null != res) {
                httpExchange.sendResponseHeaders(200, 0);
                httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                try (OutputStream out = httpExchange.getResponseBody()) {
                    out.write(res.toString().getBytes(StandardCharsets.UTF_8));
                }
                return;// end
            }

            // static file
            if ("/".equals(path)) {
                path = "/index.html";
            }

            String webFile = String.format("webroot%s", path);

            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(webFile);
                 OutputStream out = httpExchange.getResponseBody()) {

                if (null == in) {
                    httpExchange.sendResponseHeaders(404, 0);
                } else {
                    httpExchange.sendResponseHeaders(200, 0);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        });
        server.start();
    }
}
