package com.example.demo.entity;

import com.example.demo.global.security.jwt.JwtRoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    private String kakaoId;

    @Column(name = "username")
    private String username;

    @Column(name = "share_code", unique = true)
    private String shareCode;

//
//    @Column(name = "nickname")
//    private String nickname;

    @Column(name = "profile_url")
    private String profileUrl;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private JwtRoleType role;
}
