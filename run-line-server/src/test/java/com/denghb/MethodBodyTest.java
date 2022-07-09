package com.denghb;

import com.denghb.runline.server.tools.SourceTools;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MethodBodyTest {

    @Test
    public void test() throws IOException {
        String filePath = "/Users/mac/IntelliJIDEAProjects/run-line/run-line-server/src/test/java/com/denghb/MethodBodyTest.java";
        List<Integer> integers = SourceTools.methodLines(filePath);

    }

    @Test
    public void test2() {
        List<String> list = new ArrayList<>();
        list.add("list.add(\"if ('\\\"' == c) {// 忽略 char a = '\\\"' 字符\");");
        list.add("if ('\"' == c) {// 忽略 char a = '\"' 字符");
        list.add("String a = \"/*\";// ss");
        list.add("//aa\nss;");
        for (String s : list) {
            String codeLine = SourceTools.removeComment(s);
            System.out.println(codeLine);
        }
    }

    @Test
    public void test3() {
        List<String> list = new ArrayList<>();
        list.add("\"\\\"\"}");
        for (String s : list) {
            String codeLine = SourceTools.removeComment(s);
            System.out.println(codeLine);
            int count = SourceTools.countKey(s, "}");
            System.out.println(count);
        }
    }

}
