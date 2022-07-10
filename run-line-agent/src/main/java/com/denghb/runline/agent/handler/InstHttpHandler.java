package com.denghb.runline.agent.handler;

import com.denghb.runline.agent.RunLineTransformer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;

public class InstHttpHandler implements HttpHandler {

    private Instrumentation inst;

    public InstHttpHandler(Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        try (OutputStream out = httpExchange.getResponseBody()) {
            if (path.contains("/..")) {
                throw new IllegalArgumentException();
            }

            String className = path.replaceFirst("/api/runline/inst/", "");
            RunLineTransformer runLineTransformer = new RunLineTransformer(className);
            inst.removeTransformer(runLineTransformer);
            inst.addTransformer(runLineTransformer, true);

            httpExchange.sendResponseHeaders(200, 0);
            out.write("ok".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println(path);
            e.printStackTrace();
        }
    }
}
