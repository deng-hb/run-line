package com.denghb.runline.server;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单注册中心
 */
@Slf4j
public class RegistryHub {

    // project#branch, List<host:port>
    private final static Map<String, Map<String, Long>> DATA = new ConcurrentHashMap<>();

    /**
     * 注册agent
     *
     * @param project
     * @param branch
     * @param host
     * @param port
     */
    public static void register(String project, String branch, String host, String port) {
        String key = String.format("%s#%s", project, branch), value = String.format("%s:%s", host, port);
        Map<String, Long> clients = DATA.computeIfAbsent(key, k -> new HashMap<>());
        clients.put(value, System.currentTimeMillis());
    }

    /**
     * 获取在线的agent
     *
     * @param project
     * @param branch
     * @return
     */
    public static List<String> getOnline(String project, String branch) {
        String key = String.format("%s#%s", project, branch);
        Map<String, Long> clients = DATA.get(key);
        List<String> list = new ArrayList<>();
        if (null != clients) {
            long now = System.currentTimeMillis();
            for (String client : clients.keySet()) {
                if (now - 30 * 1000 < clients.get(client)) {// 30s内
                    list.add(client);
                }
            }
        }
        return list;
    }

    /**
     * 获取Runline信息
     *
     * @param project
     * @param branch
     * @return
     */
    public static List<List<String>> getRunline(String project, String branch, String source) {
        String api = String.format("/api/runline/%s/%s/%s", project, branch, source);
        List<String> clients = RegistryHub.getOnline(project, branch);

        List<List<String>> list = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();// 将其他agent覆盖的也计入
        for (String client : clients) {
            String url = String.format("http://%s%s", client, api);
            String res = httpGet(url);
            if (null != res && !res.isEmpty()) {
                String[] split = res.split("\n");
                if (split.length != 2) {
                    continue;
                }
                if (list.isEmpty()) {
                    String[] allline = split[0].split(",");
                    list.add(Arrays.asList(allline));
                }
                String[] runline = split[1].split(",");
                for (String s : runline) {
                    String[] ss = s.split(":");
                    String line = ss[0];
                    Long time = Long.parseLong(ss[1]);
                    Long oldTime = map.get(line);
                    if (null == oldTime || oldTime > time) {
                        map.put(line, time);// 填入最小的时间
                    }
                }
            }
        }
        List<String> runline = new ArrayList<>();
        for (String line : map.keySet()) {
            long time = map.get(line);
            runline.add(String.format("%s;%s", line, Consts.SDF.get().format(new Date(time))));
        }
        list.add(runline);
        return list;
    }

    private static String httpGet(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            try (InputStream in = connection.getInputStream()) {

                OutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                return output.toString();

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
