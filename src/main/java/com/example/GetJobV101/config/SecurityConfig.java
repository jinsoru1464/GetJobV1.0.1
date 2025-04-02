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
                .cors()  // 🔥 CORS 설정 활성화
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()

                // ✅ Swagger, Docs 허용
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api-docs/**"
                ).permitAll()

                // ✅ 인증 API 허용
                .requestMatchers("/api/auth/**").permitAll()



                // ✅ CORS preflight 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ 정적 페이지 허용 (프론트 HTML 파일들)
                .requestMatchers(
                        "/mainpage2.html",
                        "/inputpage.html",
                        "/portfoliodetail.html",
                        "/portfoliopage.html"
                ).permitAll()

                // ✅ 포트폴리오는 인증 필요
                .requestMatchers("/api/portfolios/**").authenticated()
                .requestMatchers("/api/ai/**").authenticated()
                // ✅ AI 교정 기능은 인증 없이 허용


                // ❌ 그 외 차단
                .anyRequest().denyAll()

                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 🔥 Swagger 포함한 프론트 Origin 허용
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                //"http://localhost:8080",
                "https://getjob.world"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // ✅ 클라이언트에서 Authorization 헤더 읽을 수 있도록
        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 캐시 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
