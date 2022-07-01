package com.denghb;

/**
 * Hello world!
 */
public class App {

    static String name = "App";

    public static void main(String[] args) {

        System.out.println("Hello World!");

        System.out.println(args);

        new Thread(() -> {
            System.out.println("runnable");
        }).run();// invoke

        new Hello().say();

        new Hello2().say();
    }

    static class Hello2 {

        public void say() {
            System.out.println("hello2");
        }
    }
}

class Hello {

    public void say() {
        System.out.println("hello");
    }
}
