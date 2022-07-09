package com.denghb.runline.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHub {

    // project#branch, List<host:port>
    private final static Map<String, Map<String, Long>> DATA = new ConcurrentHashMap<>();

    public static void put(String project, String branch, String host, String port) {
        String key = String.format("%s#%s", project, branch), value = String.format("%s:%s", host, port);
        Map<String, Long> clients = DATA.computeIfAbsent(key, k -> new HashMap<>());
        clients.put(value, System.currentTimeMillis());
    }

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
}
