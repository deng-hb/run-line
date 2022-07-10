package com.denghb.runline.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class RunLineTransformer implements ClassFileTransformer {

    private final List<String> packageList = new ArrayList<>();

    public RunLineTransformer(String packages) {
        String[] ss = packages.split(",");
        for (String s : ss) {
            packageList.add(s.replaceAll("\\.", "/"));
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // System.out.println(className);
        if (className.equals(RunLineAgent.class.getName())) {
            return null;
        }
        for (String p : packageList) {
            if (className.startsWith(p)) {
                return runLineTransform(className);
            }
        }

        return null;

    }

    private byte[] runLineTransform(String className) {
        try {
            ClassReader classReader = new ClassReader(className);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassVisitor classVisitor = new RunLineClassVisitor(Opcodes.ASM5, classWriter, className);

            classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
            return classWriter.toByteArray();
        } catch (Exception e) {
            System.err.println(className);
            e.printStackTrace();
        }
        return null;
    }
}
