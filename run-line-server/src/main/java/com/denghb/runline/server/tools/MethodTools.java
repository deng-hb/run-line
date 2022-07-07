package com.denghb.runline.server.tools;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class MethodTools {

    public static List<Integer> statLine(String filePath) throws IOException {
        List<Integer> lines = new ArrayList<>();
        LineNumberReader numberReader = new LineNumberReader(new FileReader(filePath));
        String line;
        boolean methodStart = false, methodDefine = false, commentMulti = false;
        int leftBrace = 0, rightBrace = 0;// {}
        StringBuilder methodBody = new StringBuilder();
        while (null != (line = numberReader.readLine())) {
            int lineNumber = numberReader.getLineNumber();
            if (14 <= lineNumber) {
                System.out.print("");
            }
            String codeLine = removeComment(line);
            if (!commentMulti) {
                commentMulti = count(codeLine, "/*") > 0;
            }
            if (commentMulti) {
                commentMulti = count(codeLine, "*/") > 0;
                methodStart = false;
            }
            if (!methodStart && !commentMulti) {
                methodStart = count(codeLine, ")") > 0;
            }
            if (methodStart && !methodDefine) {
                methodDefine = count(codeLine, "{") > 0;
            }
            if (methodDefine && line.trim().length() > 0) {
                methodBody.append(codeLine);
                methodBody.append('\n');
                lines.add(lineNumber);
                //System.out.printf("%d > %s\n", lineNumber, line);
            } else {
                //System.out.printf("%d   %s\n", lineNumber, line);
            }
            if (methodDefine && !commentMulti) {// 计算{}的数量
                leftBrace = count(methodBody, "{");
                rightBrace = count(methodBody, "}");
            }

            if (methodDefine && leftBrace == rightBrace) {// 方法结束
                leftBrace = 0;
                rightBrace = 0;
                methodDefine = false;
                methodStart = false;
                methodBody = new StringBuilder();
            }
        }
        return lines;
    }

    /**
     * 不计算""变量内的字符
     *
     * @param key
     * @return
     */
    public static int count(CharSequence str, String key) {
        boolean isVarStr = false;
        int strLength = str.length();
        int keyLength = key.length();
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
            if (!isVarStr) {
                for (int j = 0; j < keyLength; j++) {
                    if (c != key.charAt(j)) {
                        break;
                    }
                    if (i < strLength - 1) {
                        i++;
                        c = str.charAt(i);
                    }
                    if (j == keyLength - 1) {
                        count++;
                    }
                }
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
    public static String removeComment(CharSequence str) {
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
