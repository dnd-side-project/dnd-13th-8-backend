package com.example.demo.domain.user.entity;

import com.example.demo.global.jwt.JwtRoleType;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Users extends BaseTimeEntity {

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

    public void assignShareCode(String shareCode) {
        if (this.shareCode != null) {
            throw new IllegalStateException("이미 shareCode가 존재합니다.");
        }
        this.shareCode = shareCode;
    }

    @Builder
    public Users(String kakaoId, String username, boolean enabled, JwtRoleType role, String profileUrl) {
        this.kakaoId = kakaoId;
        this.username = username;
        this.enabled = enabled;
        this.role = role;
        this.profileUrl = profileUrl;
    }
}
