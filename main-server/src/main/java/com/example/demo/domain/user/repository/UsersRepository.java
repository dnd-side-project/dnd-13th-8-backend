package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
