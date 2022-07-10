package com.denghb.runline.agent;

import com.denghb.runline.agent.handler.StatHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class RunLineAgent {

    private static String workspace;
    private static String project;
    private static String branch;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain agentArgs: " + agentArgs);

        if (null == agentArgs || agentArgs.split(";").length != 5) {
            throw new IllegalArgumentException("-javaagent:/path/run-line-agent.jar=${workspace};${project};${branch};${packages};${server}");
        }
        String[] split = agentArgs.split(";");

        workspace = String.format("%s/.runline", split[0]);
        project = split[1];
        branch = split[2];

        String packages = split[3];
        String server = split[4];
        if (!server.contains(":")) {
            server = String.format("%s:9966", server);
        }

        inst.addTransformer(new RunLineTransformer(packages), true);

        int port = 17950 + new Random().nextInt(99);
        String url = String.format("http://%s/api/registry/%s/%s/%s/%d", server, project, branch, packages, port);
        System.out.println("registry server:" + url);

        // 1s后每10s定时发送当前项目信息给服务端
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    URLConnection connection = new URL(url).openConnection();
                    InputStream inputStream = connection.getInputStream();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 10 * 1000);

        // 提供统计服务
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.createContext("/api/runline/stat", new StatHttpHandler(workspace));
            httpServer.createContext("/api/runline/clear", new StatHttpHandler(workspace));
            httpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain agentArgs: " + agentArgs);
    }

    /**
     * com/denghb/runline/server/RunLineServer/lambda$main$0/21
     *
     * @param data
     */
    public static void stat(String data) {
        String filePath = String.format("%s/%s/%s/%s", workspace, project, branch, data);
        try {
            // 创建文件夹
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            // 创建文件
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            System.err.println(data);
            System.err.println(filePath);
            e.printStackTrace();
        }
    }
}
