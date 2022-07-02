package com.denghb.runline.agent;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class RunLineHttpHandler implements HttpHandler {
    public RunLineHttpHandler(String workspace) {

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

    }
}
