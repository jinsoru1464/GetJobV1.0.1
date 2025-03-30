package com.example.GetJobV101.config;

import com.example.GetJobV101.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll() // 로그인, 회원가입은 허용
                .requestMatchers(HttpMethod.GET, "/api/portfolios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/portfolios/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/portfolios/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/portfolios/**").authenticated()

                // Swagger UI 경로와 Swagger JSON 스펙 경로 허용
                .requestMatchers("/api-docs/swagger-ui/**", "/v3/api-docs/**").permitAll()  // 수정된 Swagger UI 경로

                // mainpage2.html 경로 허용
                .requestMatchers("/mainpage2.html").permitAll()
                .requestMatchers("/inputpage.html").permitAll()
                .requestMatchers("/portfoliodetail.html").permitAll()
                .requestMatchers("/portfoliopage.html").permitAll()
                .anyRequest().denyAll()

                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}



