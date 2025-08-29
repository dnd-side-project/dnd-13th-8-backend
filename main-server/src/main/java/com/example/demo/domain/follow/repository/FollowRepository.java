package com.example.demo.domain.follow.repository;


import com.example.demo.domain.follow.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    boolean existsByUsersIdAndPlaylistId(String usersId, Long playlistId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    insert into user_follow_playlist (user_id, playlist_id, created_at, updated_at)
    select :userId, :playlistId, now(), now()
    where not exists (
        select 1 from user_follow_playlist 
        where user_id = :userId and playlist_id = :playlistId
    )
    """, nativeQuery = true)
    void insertIfNotExists(@Param("userId") String userId, @Param("playlistId") Long playlistId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUsersIdAndPlaylistId(String usersId, Long playlistId);

    void deleteByPlaylistId(Long playlistId);
}
