package com.denghb.runline.server.handler;

import com.denghb.runline.server.RegistryHub;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * 注册中心接口
 */
public class RegistryHttpHandler extends BaseHttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);
        Map<String, String> params = getParameters(httpExchange);
        String host = getRemoteHost(httpExchange);

        String project = params.get("project");
        String branch = params.get("branch");
        String port = params.get("port");

        RegistryHub.register(project, branch, host, port);

        outJson(httpExchange, "ok");
    }
}
