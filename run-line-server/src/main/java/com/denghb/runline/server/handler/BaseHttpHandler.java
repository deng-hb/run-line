package com.denghb.runline.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = getPath(httpExchange);

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
    }

    protected String getPath(HttpExchange httpExchange) {

        String path = httpExchange.getRequestURI().getPath();
        System.out.printf("http:%s\n", path);
        return path;
    }

    protected void outJson(HttpExchange httpExchange, Object res) {
        try (OutputStream out = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(200, 0);
            httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            out.write(res.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
