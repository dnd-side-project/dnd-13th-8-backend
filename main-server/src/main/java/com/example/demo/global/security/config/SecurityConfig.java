package com.example.demo.global.security.config;

import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.security.ex.CustomAccessDeniedHandler;
import com.example.demo.global.security.ex.CustomAuthenticationEntryPoint;
import com.example.demo.global.security.filter.CustomUserDetailsService;
import com.example.demo.global.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
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
                                "/api/playlist/songs",
                                "/main/playlist/search/popular",

                                // Swagger 관련 경로
                                "/main/swagger-ui/**",
                                "/main/swagger/**",
                                "/swagger/**",
                                "/main/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/actuator/health",
                                "/actuator/prometheus"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/main/mypage/**").hasAnyAuthority("ROLE_USER", "ROLE_SUPER", "ROLE_ANONYMOUS")

                        .requestMatchers("/auth/logout").authenticated()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint(authenticationEntryPoint); // 401
                    ex.accessDeniedHandler(accessDeniedHandler); // 403
                })
                .anonymous(Customizer.withDefaults());

        var jwtFilter = new JwtAuthenticationFilter(jwtProvider, userDetailsService);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
