package com.denghb.runline.server;

import java.text.SimpleDateFormat;

public interface Consts {

    String SOURCE_FOLDER = "/src/main/java/";

    ThreadLocal<SimpleDateFormat> SDF = new InheritableThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
    };
}
