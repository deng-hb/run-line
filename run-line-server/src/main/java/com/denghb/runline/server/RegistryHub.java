package com.denghb.runline.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHub {

    // project#branch, List<host:port>
    private final static Map<String, List<String>> DATA = new ConcurrentHashMap<>();

    public static void put(String project, String branch, String host, String port) {
        String key = String.format("%s#%s", project, branch), value = String.format("%s:%s", host, port);
        List<String> clients = DATA.computeIfAbsent(key, k -> new ArrayList<>());
        if (!clients.contains(value)) {
            clients.add(value);
        }
    }

    public static List<String> get(String project, String branch) {
        String key = String.format("%s#%s", project, branch);
        return DATA.get(key);
    }
}
