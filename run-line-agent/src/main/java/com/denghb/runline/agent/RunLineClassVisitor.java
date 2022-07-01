package com.denghb.runline.agent;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

public class RunLineClassVisitor extends ClassVisitor {
    private String className;

    public RunLineClassVisitor(int api, ClassVisitor classVisitor, String className) {
        super(api, classVisitor);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, descriptor, signature, exceptions);

        methodVisitor = new RunLineMethodVisitor(api, methodVisitor, className, methodName);
        return methodVisitor;
    }

    @Override
    public void visitOuterClass(String s, String s1, String s2) {
        super.visitOuterClass(s, s1, s2);
    }
}