package com.denghb.runline.agent;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class RunLine {

    static String WORKSPACE;// 工作空间
    static String PROJECT;// 项目名
    static String BRANCH;// 分支名
    static String PACKAGES;// 包名多个,分割 com.denghb.eorm,com.denghb.runline

    public final static String CLASS_PATH = RunLine.class.getName().replace(".", "/");
    public final static String METHOD_NAME = "log";
    public final static String METHOD_DESC = "(Ljava/lang/String;)V";

    private final static ThreadLocal<SimpleDateFormat> SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    /**
     * 初始化配置
     * @param agentArgs
     */
    public static void init(String agentArgs) {
        if (null == agentArgs || agentArgs.split(";").length != 4) {
            throw new IllegalArgumentException("-javaagent:/path/run-line-agent.jar=${workspace};${project};${branch};${packages}");
        }
        String[] split = agentArgs.split(";");

        WORKSPACE = split[0];
        PROJECT = split[1];
        BRANCH = split[2];
        PACKAGES = split[3];
    }

    /**
     * com/denghb/runline/server/RunLineServer#lambda$main$0:21
     *
     * @param message
     */
    public static void log(String message) {
        System.out.println(message);

        // TODO async
        String[] split = message.split("#");
        String className = split[0];
        String methodLine = split[1];

        String date = SDF.get().format(new Date());

        String filePath = String.format("%s/.runline/%s/%s/%s", WORKSPACE, PROJECT, BRANCH, className);
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter fileWriter = new FileWriter(file, true)) {
            String content = String.format("%s#%s\n", date, methodLine);
            fileWriter.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
