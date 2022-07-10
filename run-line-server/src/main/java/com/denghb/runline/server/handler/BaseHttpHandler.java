package com.denghb.runline.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        log.info("{}:{}", remoteHost, httpExchange.getRequestURI());
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

    protected Map<String, String> getParameters(HttpExchange httpExchange) {
        String query = httpExchange.getRequestURI().getQuery();
        Map<String, String> result = new HashMap<>();
        if (query == null || query.trim().length() == 0) {
            return result;
        }
        String[] items = query.split("&");
        Arrays.stream(items).forEach(item -> {
            String[] keyAndVal = item.split("=");
            if (keyAndVal.length == 2) {
                try {
                    String key = URLDecoder.decode(keyAndVal[0], "utf8");
                    String val = URLDecoder.decode(keyAndVal[1], "utf8");
                    result.put(key, val);
                } catch (UnsupportedEncodingException e) {
                }
            }
        });
        return result;
    }
}
