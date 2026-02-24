package com.example.demo.domain.like.repository;

import com.example.demo.domain.like.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikesRepository extends JpaRepository<Likes, Long>, LikesRepositoryCustom {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        insert into likes (user_id, playlist_id, created_at, updated_at)
        select :userId, :playlistId, now(), now()
        where not exists (
            select 1
            from likes
            where user_id = :userId
              and playlist_id = :playlistId
        )
        """, nativeQuery = true)
    void insertIfNotExists(@Param("userId") String userId,
                           @Param("playlistId") Long playlistId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUsers_IdAndPlaylist_Id(String userId, Long playlistId);

    boolean existsByUsers_IdAndPlaylist_Id(String usersId, Long playlistId);

    long countByUsers_Id(String userId);
}
