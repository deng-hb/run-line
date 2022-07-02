package com.denghb.runline.server;


import com.denghb.runline.server.handler.BaseHttpHandler;
import com.denghb.runline.server.handler.GitOperateHttpHandler;
import com.denghb.runline.server.handler.ProjectHttpHandler;
import com.denghb.runline.server.handler.RegistryHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
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
        server.createContext("/", new BaseHttpHandler());
        server.createContext("/git", new GitOperateHttpHandler());
        server.createContext("/project", new ProjectHttpHandler());
        server.createContext("/registry", new RegistryHttpHandler());
        server.start();
    }
}
