package com.denghb.runline.server;


import com.denghb.runline.server.handler.*;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

@Slf4j
public class RunLineServer {

    public static String WORKSPACE;

    public static void main(String[] args) throws IOException {
        log.info("args:{}", Arrays.toString(args));
        if (args.length < 1 || args[0].endsWith("/")) {
            throw new IllegalArgumentException("java -jar run-line-server.jar ${workspace} ${port}");
        }
        int port = 9966;
        if (args.length == 2) {
            port = Integer.parseInt(args[1]);
        }
        WORKSPACE = String.format("%s/runline", args[0]);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.createContext("/", new IndexHttpHandler());
        server.createContext("/api/git", new GitOperateHttpHandler());
        server.createContext("/api/project", new ProjectHttpHandler());
        server.createContext("/api/registry", new RegistryHttpHandler());
        server.createContext("/api/runline", new RunLineHttpHandler());
        server.start();
    }
}
