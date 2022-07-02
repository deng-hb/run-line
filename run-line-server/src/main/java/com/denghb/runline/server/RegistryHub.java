package com.denghb.runline.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHub {

    // project#branch, List<host:port>
    private final static Map<String, List<String>> DATA = new ConcurrentHashMap<>();

    public static void put(String key, String value) {
        List<String> clients = DATA.computeIfAbsent(key, k -> new ArrayList<>());
        if (!clients.contains(value)) {
            clients.add(value);
        }
    }
}
