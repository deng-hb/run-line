package com.denghb.runline.agent;

import java.lang.instrument.Instrumentation;

public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("premain agentArgs: " + agentArgs);
        if (null == agentArgs || agentArgs.split(";").length != 3) {
            throw new IllegalArgumentException("-javaagent:/run-line-agent.jar=${data dir};${git branch};${base packages}");
        }
        String[] split = agentArgs.split(";");

        RunLine.WORKSPACE = split[0];
        RunLine.BRANCH_NAME = split[1];
        RunLine.PACKAGES = split[2];

        inst.addTransformer(new RunLineTransformer(), true);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain agentArgs : " + agentArgs);
    }
}
