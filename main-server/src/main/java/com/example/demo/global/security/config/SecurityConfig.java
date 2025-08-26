package com.example.demo.global.security.config;


import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.security.filter.CustomUserDetailsService;
import com.example.demo.global.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/auth/anonymous",
                                "/auth/login",
                                "/auth/super",
                                "/auth/kakao/**",
                                "/auth/session",
                                "/health",
                                "/chat/health",
                                "/api/health",

                                //  Swagger 관련 경로
                                "/main/swagger-ui/**",
                                "/main/swagger/**",
                                "/swagger/**",
                                "/main/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        //  마이페이지는 user/super 권한만 허용
                        .requestMatchers("/main/mypage/**").hasAnyAuthority("ROLE_USER", "ROLE_SUPER")

                        .requestMatchers("/auth/logout").authenticated()
                        // 그 외 모든 요청은 인증만 필요
                        .anyRequest().authenticated()

                )
                .anonymous(Customizer.withDefaults());

        var jwtFilter = new JwtAuthenticationFilter(jwtProvider, userDetailsService);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
