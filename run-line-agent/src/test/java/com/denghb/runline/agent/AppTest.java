package com.denghb.runline.agent;

import static org.junit.Assert.assertTrue;

import com.denghb.Hello;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.*;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void test() throws Exception {

        String className = Hello.class.getName().replaceAll("\\.", "/");

        ClassReader classReader = new ClassReader(className);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new RunLineClassVisitor(Opcodes.ASM5, classWriter, className);

        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        byte[] bytes = classWriter.toByteArray();
        Class clazz = new RunLineClassLoader().classWithBytes(bytes);

        Object o = clazz.newInstance();
        Method setName = clazz.getMethod("setName", String.class);
        setName.invoke(o, "denghb");

        Method getName = clazz.getMethod("getName");
        System.out.println(getName.invoke(o));
    }
}

class RunLineClassLoader extends ClassLoader {

    public Class<?> classWithBytes(byte[] bytes) {
        return defineClass(null, bytes, 0, bytes.length);
    }

}

