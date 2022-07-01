package com.denghb.runline.agent;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

public class RunLineMethodVisitor extends MethodVisitor {

    private String className;
    private String methodName;

    public RunLineMethodVisitor(int api, MethodVisitor methodVisitor, String className, String methodName) {
        super(api, methodVisitor);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public void visitLineNumber(int line, Label label) {
        super.visitLineNumber(line, label);

        super.visitLdcInsn(String.format("%s#%s:%d", className, methodName, line));
        super.visitMethodInsn(Opcodes.INVOKESTATIC, RunLine.CLASS_PATH, RunLine.METHOD_NAME, RunLine.METHOD_DESC, false);
    }
}
