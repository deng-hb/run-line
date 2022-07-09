package com.denghb;

public class Hello {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static class InnerClass {

        private String a;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }
    }
}

class OuterClass {
    private String b;

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }
}
