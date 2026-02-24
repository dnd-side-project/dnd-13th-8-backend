package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Users;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, String>, UserRepositoryCustom{

    Optional<Users> findByShareCode(String shareCode);

    boolean existsByUsername(String nickname);

    Optional<Users> findByKakaoId(String kakaoId);
}
