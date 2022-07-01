package com.denghb.runline.server;


import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class RunLineServer {

    public static void main(String[] args) throws IOException {
        System.out.println(args);
        System.out.println(System.getenv());
        HttpServer server = HttpServer.create(new InetSocketAddress(9966), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", (httpExchange) -> {
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = httpExchange.getResponseBody();

            responseBody.write("ok".getBytes(StandardCharsets.UTF_8));
            responseBody.close();

        });
        server.start();
    }
}
