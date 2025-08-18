package com.example.demo.global.security.config;

import com.example.demo.global.http.HttpOnlyCookieUtil;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.security.filter.CustomUserDetailsService;
import com.example.demo.global.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final HttpOnlyCookieUtil cookieUtil;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/cd/**")) // refresh 붙이기 전 단계
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        // .requestMatchers("/**").permitAll() // 개발용 임시
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/super").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/chat/health").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/auth/kakao/**").permitAll()
                        .requestMatchers("/auth/session").permitAll()
                        .requestMatchers("/auth/logout").authenticated()
                        .anyRequest().authenticated()
                )
                .anonymous(Customizer.withDefaults());

        var jwtFilter = new JwtAuthenticationFilter(jwtProvider, userDetailsService, cookieUtil);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
