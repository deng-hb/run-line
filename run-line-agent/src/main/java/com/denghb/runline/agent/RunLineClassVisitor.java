package com.denghb.runline.agent;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

public class RunLineClassVisitor extends ClassVisitor {

    private final static String STAT_CLASS = RunLineAgent.class.getName().replace(".", "/");

    private String className;

    public RunLineClassVisitor(int api, ClassVisitor classVisitor, String className) {
        super(api, classVisitor);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, descriptor, signature, exceptions);
        methodVisitor = new MethodVisitor(api, methodVisitor) {
            @Override
            public void visitLineNumber(int line, Label label) {
                super.visitLineNumber(line, label);

                super.visitLdcInsn(String.format("%s/%s/%d", className, methodName, line));
                super.visitMethodInsn(Opcodes.INVOKESTATIC, STAT_CLASS, "stat", "(Ljava/lang/String;)V", false);
            }
        };
        return methodVisitor;
    }

    @Override
    public void visitOuterClass(String s, String s1, String s2) {
        super.visitOuterClass(s, s1, s2);
    }
}