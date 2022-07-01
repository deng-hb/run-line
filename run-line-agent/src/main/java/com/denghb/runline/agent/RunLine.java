package com.denghb.runline.agent;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RunLine {

    public static String WORKSPACE = System.getProperty("user.home");
    public static String BRANCH_NAME = "master";
    public static String PACKAGES = "";

    public final static String CLASS_PATH = RunLine.class.getName().replace(".", "/");
    public final static String METHOD_NAME = "log";
    public final static String METHOD_DESC = "(Ljava/lang/String;)V";

    /**
     * com/denghb/runline/server/RunLineServer#lambda$main$0:21
     *
     * @param message
     */
    public static void log(String message) {
        System.out.println(message);

        // async
        String[] split = message.split("#");

        String className = split[0];
        String methodLine = split[1];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String date = sdf.format(new Date());

        String filePath = String.format("%s/runline/%s/%s", WORKSPACE, BRANCH_NAME, className);
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
