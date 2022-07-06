package com.denghb;

import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class MethodBodyTest {

    @Test
    public void test() throws IOException {
        String filePath = "/Users/mac/IntelliJIDEAProjects/run-line/run-line-server/src/test/java/com/denghb/MethodBodyTest.java";
        LineNumberReader numberReader = new LineNumberReader(new FileReader(filePath));
        String line;
        boolean methodStart = false, methodDefine = false, commentMulti = false;
        int leftBrace = 0, rightBrace = 0;// {}
        StringBuilder methodBody = new StringBuilder();
        while (null != (line = numberReader.readLine())) {
            int lineNumber = numberReader.getLineNumber();
            if (193 <= lineNumber) {
                System.out.print("");
            }
            String codeLine = removeComment(line);
            if (!commentMulti) {
                commentMulti = codeLine.contains("/*");
            }
            if (commentMulti) {
                commentMulti = codeLine.contains("*/");
                methodStart = false;
            }
            if (!methodStart && !commentMulti) {
                methodStart = codeLine.contains(")");
            }
            if (methodStart && !methodDefine) {
                methodDefine = codeLine.contains("{");
            }
            if (methodDefine && line.trim().length() > 0) {
                methodBody.append(codeLine);
                methodBody.append('\n');

                System.out.printf("%d > %s\n", lineNumber, line);
            } else {
                System.out.printf("%d   %s\n", lineNumber, line);
            }
            if (methodDefine && !commentMulti) {// 计算{}的数量
                leftBrace = count(methodBody, '{');
                rightBrace = count(methodBody, '}');
            }

            if (methodDefine && leftBrace == rightBrace) {// 方法结束
                leftBrace = 0;
                rightBrace = 0;
                methodDefine = false;
                methodStart = false;
                methodBody = new StringBuilder();
            }
        }
    }

    @Test
    public void test2() {
        List<String> list = new ArrayList<>();
        list.add("list.add(\"if ('\\\"' == c) {// 忽略 char a = '\\\"' 字符\");");
        list.add("if ('\"' == c) {// 忽略 char a = '\"' 字符");
        list.add("String a = \"/*\";// ss");
        list.add("//aa\nss;");
        for (String s : list) {
            String codeLine = removeComment(s);
            System.out.println(codeLine);
        }
    }

    @Test
    public void test3() {
        List<String> list = new ArrayList<>();
        list.add("\"\\\"\"}");
        for (String s : list) {
            String codeLine = removeComment(s);
            System.out.println(codeLine);
            int count = count(s, '}');
            System.out.println(count);
        }
    }

    /**
     * 不计算""变量内的字符
     *
     * @param key
     * @return
     */
    private int count(CharSequence str, char key) {
        boolean isVarStr = false;
        int strLength = str.length();
        int count = 0;
        for (int i = 0; i < strLength; i++) {
            char c = str.charAt(i);
            // String s = "\""; 字符
            if ('\\' == c && i < strLength - 1 && '"' == str.charAt(i + 1)) {
                i++;
                continue;
            }
            // char a = '"' 字符
            if ('\'' == c && i < strLength - 2 && '"' == str.charAt(i + 1) && '\'' == str.charAt(i + 2)) {
                i += 2;
                continue;
            }
            if ('"' == c) {// 忽略 char a = '"' 字符
                isVarStr = !isVarStr;
            }
            if (!isVarStr && c == key) {
                count++;
            }
        }
        return count;
    }

    /**
     * 移除注释
     *
     * @param str
     * @return
     */
    private String removeComment(CharSequence str) {
        StringBuilder sb = new StringBuilder();
        if (null == str) {
            return sb.toString();
        }
        List<Character> charList = new ArrayList<>();
        boolean isVarStr = false;
        int strLength = str.length();
        for (int i = 0; i < strLength; i++) {
            char c1 = str.charAt(i);
            // String s = "\""; 字符
            if ('\\' == c1 && i < strLength - 1 && '"' == str.charAt(i + 1)) {
                sb.append("\\\"");
                i++;
                continue;
            }
            // char a = '"' 字符
            if ('\'' == c1 && i < strLength - 2 && '"' == str.charAt(i + 1) && '\'' == str.charAt(i + 2)) {
                sb.append("'\"'");
                i += 2;
                continue;
            }
            if ('"' == c1) {
                isVarStr = !isVarStr;
            }
            // char a = '/' 字符
            if (!isVarStr && '/' == c1 && i < strLength - 1 && '\'' != str.charAt(i + 1)) {
                i++;
                char c2 = str.charAt(i);
                if ('/' == c2) {// String a = "";// xxxx
                    for (; i < strLength; i++) {
                        char c3 = str.charAt(i);
                        if ('\n' == c3) {
                            break;// 之后的全部忽略
                        }
                    }
                } else if ('*' == c2) {// 多行注释/* 先暂存，等遇到*/再清理，没遇到*/就拼接回去
                    charList.add(c1);
                    charList.add(c2);
                    i++;
                    for (; i < strLength; i++) {
                        char c3 = str.charAt(i);
                        charList.add(c3);
                        if ('*' == c3 && i < strLength - 1) {// */ 结束
                            i++;
                            char c4 = str.charAt(i);
                            if ('/' == c4) {
                                charList.clear();
                                break;
                            }
                            charList.add(c4);
                        }
                    }//  for end
                    for (Character c : charList) {
                        sb.append(c);
                    }
                } else {
                    sb.append(c1);
                }
            } else {
                sb.append(c1);
            }

        }
        return sb.toString();
    }
}
