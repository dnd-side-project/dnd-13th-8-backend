package com.example.demo.global.security.filter;

import com.example.common.error.exception.JwtException;
import com.example.demo.global.http.dto.CookieProps;
import com.example.demo.global.http.util.CookieReader;
import com.example.demo.global.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CookieReader cookieReader;
    private final CookieProps props;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        var accessOpt = cookieReader.read(req, props.access().name());
        if (accessOpt.isPresent()) {
            try {
                var jws = jwtProvider.validateAccess(accessOpt.get());
                String userId = jws.getPayload().getSubject();

                var ud = userDetailsService.loadUserByUsername(userId);
                var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException ex) {
                SecurityContextHolder.clearContext();
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                throw ex;
            }
        }
        chain.doFilter(req, res);
    }
}
