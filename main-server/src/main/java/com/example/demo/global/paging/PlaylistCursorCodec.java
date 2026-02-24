package com.example.demo.global.paging;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlaylistCursorCodec {
    private static final String SEP = "|";

    public static PlaylistCursor decode(String opaqueCursor, PlaylistSortOption requestSort) {
        if (opaqueCursor == null || opaqueCursor.isBlank()) return null;

        try {
            String raw = new String(Base64.getUrlDecoder().decode(opaqueCursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", -1);
            if (parts.length != 3) throw new IllegalArgumentException("Invalid cursor");

            PlaylistSortOption cursorSort = PlaylistSortOption.valueOf(parts[0]);
            if (requestSort == null || cursorSort != requestSort) return null;

            Long id = parts[1].isBlank() ? null : Long.parseLong(parts[1]);
            Long visitCount = parts[2].isBlank() ? null : Long.parseLong(parts[2]);

            return new PlaylistCursor(id, visitCount);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    public static String encode(PlaylistSortOption sort, PlaylistCursor cursor) {
        if (sort == null || cursor == null || cursor.id() == null) return null;

        String raw = sort.name()
                + SEP + cursor.id()
                + SEP + (cursor.visitCount() == null ? "" : cursor.visitCount());

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
