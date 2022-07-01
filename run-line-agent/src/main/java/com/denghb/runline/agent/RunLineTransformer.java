package com.denghb.runline.agent;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunLineTransformer implements ClassFileTransformer {

    private List<String> packageList = new ArrayList<>();

    public RunLineTransformer(String packages) {
        String[] ss = packages.split(",");
        for (String s : ss) {
            String s1 = s.replaceAll("\\.", "/");
            packageList.add(s1);
        }

    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // System.out.println(className);

        for (String p : packageList) {
            if (className.startsWith(p) && !className.equals(RunLine.class.getName())) {
                try {
                    ClassReader classReader = new ClassReader(className);
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    ClassVisitor classVisitor = new RunLineClassVisitor(Opcodes.ASM5, classWriter, className);

                    classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
                    return classWriter.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;

    }
}
