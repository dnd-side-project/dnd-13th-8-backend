package com.example.demo.domain.browse.repository;

import com.example.demo.domain.playlist.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface BrowsePlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query(value = """
  SELECT p.id
  FROM playlist p
  WHERE p.is_public = TRUE
    AND p.user_id <> :userId
  ORDER BY
    CAST(CONV(SUBSTR(SHA2(CONCAT(:userId, :seedKey, p.id), 256), 1, 16), 16, 10) AS UNSIGNED) ASC,
    p.id ASC
  LIMIT :limitPlusOne
  """, nativeQuery = true)
    List<Long> findFirstPageIdsShuffledExcludeMine(
            @Param("userId") String userId,
            @Param("seedKey") String seedKey,
            @Param("limitPlusOne") int limitPlusOne
    );

    @Query(value = """
  SELECT p.id
  FROM playlist p
  WHERE p.is_public = TRUE
    AND p.user_id <> :userId
    AND (
      CAST(CONV(SUBSTR(SHA2(CONCAT(:userId, :seedKey, p.id), 256), 1, 16), 16, 10) AS UNSIGNED)
        > CAST(CONV(SUBSTR(SHA2(CONCAT(:userId, :seedKey, :cursorId), 256), 1, 16), 16, 10) AS UNSIGNED)
      OR (
        CAST(CONV(SUBSTR(SHA2(CONCAT(:userId, :seedKey, p.id), 256), 1, 16), 16, 10) AS UNSIGNED)
          = CAST(CONV(SUBSTR(SHA2(CONCAT(:userId, :seedKey, :cursorId), 256), 1, 16), 16, 10) AS UNSIGNED)
        AND p.id > :cursorId
      )
    )
  ORDER BY
    CAST(CONV(SUBSTR(SHA2(CONCAT(:userId, :seedKey, p.id), 256), 1, 16), 16, 10) AS UNSIGNED) ASC,
    p.id ASC
  LIMIT :limitPlusOne
  """, nativeQuery = true)
    List<Long> findNextPageIdsShuffledExcludeMine(
            @Param("userId") String userId,
            @Param("seedKey") String seedKey,
            @Param("cursorId") Long cursorId,
            @Param("limitPlusOne") int limitPlusOne
    );
}
