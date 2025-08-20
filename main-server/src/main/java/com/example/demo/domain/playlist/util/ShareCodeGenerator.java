package com.example.demo.domain.playlist.util;

import java.util.UUID;

public class ShareCodeGenerator {
    public static String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }
}
