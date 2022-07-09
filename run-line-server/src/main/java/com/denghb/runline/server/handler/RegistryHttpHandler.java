package com.denghb.runline.server.handler;

import com.denghb.runline.server.RegistryHub;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class RegistryHttpHandler extends BaseHttpHandler {
    @Override
    public Object handle(String path) {
        return null;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);
        String host = getRemoteHost(httpExchange);
        String[] split = path.split("/");

        String project = split[2];
        String branch = split[3];
        String packages = split[4];
        String port = split[5];

        RegistryHub.register(project, branch, host, port);

        outJson(httpExchange, "ok");
    }
}
