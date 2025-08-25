package com.example.demo.domain.playlist.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class ShareCodeGenerator {

    private static final int LENGTH = 8; // 원하는 길이

    public static String generate(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must be a non-empty UUID string");
        }

        UUID uuid = UUID.fromString(userId.trim());
        byte[] bytes = toBytes(uuid);

        String b64 = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        return b64.substring(0, LENGTH);
    }

    private static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
