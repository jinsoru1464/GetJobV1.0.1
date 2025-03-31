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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors() // 🔥 CORS 활성화
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/portfolios/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/portfolios/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/portfolios/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/portfolios/**").authenticated()

                .requestMatchers("/api-docs/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/mainpage2.html").permitAll()
                .requestMatchers("/inputpage.html").permitAll()
                .requestMatchers("/portfoliodetail.html").permitAll()
                .requestMatchers("/portfoliopage.html").permitAll()
                .anyRequest().denyAll()

                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🔥 CORS 설정 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:8080", // 개발용
                "https://getjob.world"  // 배포용 (https로 쓰는 게 좋아)
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 인증정보 허용
        config.setMaxAge(3600L); // preflight 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}