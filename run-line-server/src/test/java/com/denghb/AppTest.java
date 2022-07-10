package com.denghb;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
    public void test() {


        String s = "java/lang/invoke/MethodHandles$Lookup";
        String s2 = s.replaceFirst("\\\\$", "/$");
        System.out.println(s2);
    }
}
