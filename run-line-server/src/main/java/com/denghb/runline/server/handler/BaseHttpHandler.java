package com.denghb.runline.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class BaseHttpHandler implements HttpHandler {

    public abstract Object handle(String path) throws Exception;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        long start = System.currentTimeMillis();
        String path = getPath(httpExchange);
        try {
            Object res = handle(path);
            if (null != res) {
                outJson(httpExchange, res);
                log.info("{}:{}ms", path, (System.currentTimeMillis() - start));
                return;
            }
        } catch (Exception e) {
            log.error(String.format("\n%s\n%s", path, e.getMessage()), e);
        }
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
            log.error(e.getMessage(), e);
        }
    }

    protected String getPath(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        String remoteHost = getRemoteHost(httpExchange);
        if (path.contains("/..")) {
            log.error("{}:{}", remoteHost, path);
            throw new IllegalArgumentException("Illegal Argument");
        }
        log.info("{}:{}", remoteHost, path);
        // remove "/api"
        return path.replaceFirst("/api", "");
    }

    protected String getRemoteHost(HttpExchange httpExchange) {
        String host = httpExchange.getRemoteAddress().getAddress().getHostAddress();
        return host;
    }

    protected void outJson(HttpExchange httpExchange, Object res) {
        try (OutputStream out = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(200, 0);
            httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            out.write(res.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
