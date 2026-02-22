package com.example.demo.domain.user.utils;

import java.security.SecureRandom;

public class ShareCodeGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String CHAR_POOL =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";

    private static final int LENGTH = 8;

    public static String generate() {
        StringBuilder s = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            s.append(CHAR_POOL.charAt(index));
        }

        return s.toString();
    }
}
