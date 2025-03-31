package com.example.GetJobV101.jwt;

import com.example.GetJobV101.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String loginId = jwtUtil.getLoginIdFromToken(token);

        if (loginId == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ TEMP USER 처리
        if (loginId.equals("temporary")) {
            if (!jwtUtil.isTokenExpired(token)) {
                UsernamePasswordAuthenticationToken tempAuth =
                        new UsernamePasswordAuthenticationToken("temporary", null,
                                List.of(() -> "ROLE_TEMP_USER")); // 람다로 권한 생성
                SecurityContextHolder.getContext().setAuthentication(tempAuth);
            }
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 일반 USER 처리
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

        if (jwtUtil.validateToken(token, userDetails)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

}
