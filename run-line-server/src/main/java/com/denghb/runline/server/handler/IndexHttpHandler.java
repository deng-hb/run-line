package com.denghb.runline.server.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class IndexHttpHandler extends BaseHttpHandler{
    @Override
    public Object handle(String path) throws Exception {
        return null;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        super.handle(httpExchange);
    }
}
