package org.voidlang.compiler.util;

public class Validate {
    public static void panic(String message) {
        System.err.printf("Thread %s panic: %s%n", Thread.currentThread().getName(), message);
        System.exit(-1);
    }
}
