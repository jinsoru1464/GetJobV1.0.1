package com.example.GetJobV101.jwt;

import com.example.GetJobV101.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("🚨 Filter check: " + path);

        // Swagger UI와 인증 API는 JWT 필터 제외
        if (path.startsWith("/api/auth")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")) {
            System.out.println("✅ JWT 필터 제외: " + path);
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("🧾 Authorization 헤더: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("❌ Authorization 헤더 없음 또는 잘못된 형식");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        System.out.println("📦 받은 토큰: " + token);

        try {
            String loginId = jwtUtil.getLoginIdFromToken(token);
            System.out.println("🔑 추출된 loginId: " + loginId);

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("🧾 현재 SecurityContext 인증: " + currentAuth);

            if (loginId != null && currentAuth == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

                if (userDetails == null) {
                    System.out.println("🚫 유저 정보를 찾을 수 없음 (UserDetails null)");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"🚫 유저 정보를 찾을 수 없습니다.\"}");
                    return;
                } else if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("🔒 SecurityContext 인증 설정 완료: " + userDetails.getUsername());
                } else {
                    System.out.println("🚫 토큰 유효성 검사 실패");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"🚫 유효하지 않은 토큰입니다.\"}");
                    return;
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("⛔ 토큰 만료됨: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"⛔ 토큰이 만료되었습니다. 다시 로그인 해주세요.\"}");
            return;

        } catch (io.jsonwebtoken.JwtException e) {
            System.out.println("❌ 잘못된 토큰: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"🚫 유효하지 않은 토큰입니다.\"}");
            return;

        } catch (Exception e) {
            System.out.println("❓ 기타 예외: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"서버 오류가 발생했습니다.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
