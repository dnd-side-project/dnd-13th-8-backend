package com.example.demo.global.paging;

import java.util.List;

public record BiCursorPageResponse<T, C>(
        List<T> content,
        C prevCursor,
        C nextCursor,
        int size,
        boolean hasPrev,
        boolean hasNext
) {
}
