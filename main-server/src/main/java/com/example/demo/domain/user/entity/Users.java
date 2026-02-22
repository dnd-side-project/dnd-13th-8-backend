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

    @Column(name = "bio")
    private String bio;

//    @Column(name = "nickname")
//    private String nickname;

    @Column(name = "profile_url")
    private String profileUrl;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private JwtRoleType role;

    @Builder
    public Users(String kakaoId, String username, boolean enabled, JwtRoleType role, String profileUrl, String shareCode) {
        this.kakaoId = kakaoId;
        this.username = username;
        this.enabled = enabled;
        this.role = role;
        this.profileUrl = profileUrl;
        this.shareCode = shareCode;
    }

    // 닉네임 변경 메서드
    public void changeNickname(String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }
        this.username = newNickname;
    }

    public void changeBio(String newBio) {
        this.bio = newBio;
    }

    public void changeShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    // 프로필 이미지 변경 메서드
    public void changeProfileImage(String newProfileImageUrl) {
        this.profileUrl = newProfileImageUrl;
    }
}
