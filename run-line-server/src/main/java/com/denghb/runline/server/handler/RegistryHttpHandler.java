package com.denghb.runline.server.handler;

import com.denghb.runline.server.RegistryHub;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class RegistryHttpHandler extends BaseHttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);

        String host = httpExchange.getRemoteAddress().getAddress().getHostAddress();
        String[] split = path.split("/");
        String project = split[2];
        String branch = split[3];
        String packages = split[4];
        String port = split[5];
        RegistryHub.put(String.format("%s#%s", project, branch), String.format("%s:%s", host, port));

        outJson(httpExchange, "ok");
    }
}
