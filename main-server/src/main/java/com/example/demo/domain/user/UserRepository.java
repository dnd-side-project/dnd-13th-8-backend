package com.example.demo.domain.user;

import com.example.demo.domain.user.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, String> {
    
    Users findByUserId(String username);
}
