package com.example.demo.domain.user.utils;

import java.security.SecureRandom;

public class ShareCodeGenerator {

    private static final int LENGTH = 8;

    private static final SecureRandom random = new SecureRandom();
    private static final int MAX = 100_000_000;

    public static String generate() {
        int number = random.nextInt(MAX);
        return String.format("%08d", number);
    }
}
