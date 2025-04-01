package com.example.GetJobV101.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SwaggerDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String origin = request.getHeader("Origin");

        if (uri.contains("swagger") || uri.contains("api-docs")) {
            System.out.println("🟢 Swagger 요청 감지!");
            System.out.println("▶ URI: " + uri);
            System.out.println("▶ Method: " + request.getMethod());
            System.out.println("▶ Origin: " + origin);
        }

        filterChain.doFilter(request, response);
    }
}
