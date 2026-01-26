package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersRepository extends JpaRepository<Users, String>, UserRepositoryCustom{

    Optional<Users> findUsersByShareCode(String shareCode);

    boolean existsByUsername(String nickname);

    Optional<Users> findByKakaoId(String kakaoId);
}
