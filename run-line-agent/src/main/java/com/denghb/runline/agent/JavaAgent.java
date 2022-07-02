package com.denghb.runline.agent;

import java.lang.instrument.Instrumentation;

public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain agentArgs: " + agentArgs);

        RunLine.init(agentArgs);
        inst.addTransformer(new RunLineTransformer(), true);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain agentArgs: " + agentArgs);
    }
}
