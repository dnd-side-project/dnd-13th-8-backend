package com.example.demo.global.security.filter;

import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;

    /**
     * username 파라미터를 우선 userId로 보고 조회, 없으면 email로 재조회
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        if (userId == null || userId.isBlank()) {
            throw new UsernameNotFoundException("사용자명이 비어 있습니다.");
        }
        Users opt = userRepository.findById(userId).orElseThrow(()-> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        return new CustomUserDetails(opt);
    }
}