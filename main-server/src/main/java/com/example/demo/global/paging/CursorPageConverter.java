package com.example.demo.global.paging;

import com.example.demo.domain.playlist.dto.page.CursorPageResponse;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CursorPageConverter {

        public static <E, D, C> CursorPageResponse<D, C> toCursorResponse(
                List<E> entities,
                int limit,
                Function<E, D> mapper,
                Function<D, C> cursorExtractor,
                long totalCount) {
            boolean hasNext = entities.size() > limit;
            if (hasNext) {
                entities = entities.subList(0, limit);
            }

            List<D> dtoList = entities.stream()
                    .map(mapper)
                    .collect(Collectors.toList());

            C nextCursor = null;
            if (hasNext && !dtoList.isEmpty()) {
                nextCursor = cursorExtractor.apply(dtoList.get(dtoList.size() - 1));
            }

            return new CursorPageResponse<>(
                    dtoList,
                    nextCursor,
                    dtoList.size(),
                    hasNext,
                    totalCount
            );
        }
    }


