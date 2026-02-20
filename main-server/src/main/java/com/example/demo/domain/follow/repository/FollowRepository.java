package com.example.demo.domain.follow.repository;


import com.example.demo.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    boolean existsByFollower_IdAndFollowee_Id(String followerId, String followeeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    insert into follow (follower_id, followee_id, created_at, updated_at)
    select :followerId, :followeeId, now(), now()
    where not exists (
        select 1
        from follow
        where follower_id = :followerId
          and followee_id = :followeeId
    )
    """, nativeQuery = true)
    void insertIfNotExists(@Param("followerId") String followerId,
                           @Param("followeeId") String followeeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByFollower_IdAndFollowee_Id(String followerId, String followeeId);

    @Query("select count(f) from Follow f where f.followee.id = :userId")
    long countFollowerByUsers_Id(@Param("userId") String userId);

    @Query("select count(f) from Follow f where f.follower.id = :userId")
    long countFolloweeByUsers_Id(@Param("userId") String userId);
}
