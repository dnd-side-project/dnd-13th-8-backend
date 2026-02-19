package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.UserMusicKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMusicKeywordRepository extends JpaRepository<UserMusicKeyword, String> {

    void deleteByUsersId(String usersId);
}
