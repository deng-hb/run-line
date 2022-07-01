package com.denghb.runline.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain agentArgs: " + agentArgs);
        String propertiesPath = agentArgs;
        if (null == agentArgs || agentArgs.length() == 0) {
            propertiesPath = System.getProperty("user.home") + "/runline.properties";
        }
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesPath));
            String packages = properties.getProperty("packages");
            inst.addTransformer(new RunLineTransformer(packages), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain agentArgs : " + agentArgs);
    }
}
