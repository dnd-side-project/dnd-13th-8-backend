package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.MusicKeyword;
import com.example.demo.domain.user.entity.UserMusicKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserMusicKeywordRepository extends JpaRepository<UserMusicKeyword, Long> {

    void deleteByUsers_Id(String usersId);

    @Query("select umk.musicKeyword from UserMusicKeyword umk where umk.users.id = :userId")
    List<MusicKeyword> findAllKeywordsByUsers_Id(@Param("userId") String userId);
}
