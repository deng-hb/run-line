package com.denghb.runline.agent;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

public class RunLineClassVisitor extends ClassVisitor {

    private final static String STAT_CLASS = RunLineAgent.class.getName().replace(".", "/");

    private final String className;

    public RunLineClassVisitor(int api, ClassVisitor classVisitor, String className) {
        super(api, classVisitor);
        this.className = className.replace("$", "/$");
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, descriptor, signature, exceptions);
        methodVisitor = new MethodVisitor(api, methodVisitor) {
            @Override
            public void visitLineNumber(int line, Label label) {
                super.visitLineNumber(line, label);

                // 插桩，目标程序运行会执行
                super.visitLdcInsn(String.format("%s/%s/%d", className, methodName, line));
                super.visitMethodInsn(Opcodes.INVOKESTATIC, STAT_CLASS, "stat", "(Ljava/lang/String;)V", false);

                // 给目标插桩时执行，记录行
                RunLineAgent.stat(String.format("%s/.java/%d", className, line));
            }


        };
        return methodVisitor;
    }

    @Override
    public void visitInnerClass(String s, String s1, String s2, int i) {
        super.visitInnerClass(s, s1, s2, i);
        System.out.printf("%s\t%s\t%s\t%d%n", s, s1, s2, i);
    }

    @Override
    public void visitOuterClass(String s, String s1, String s2) {
        super.visitOuterClass(s, s1, s2);
        System.out.printf("%s\t%s\t%s%n", s, s1, s2);
    }


}