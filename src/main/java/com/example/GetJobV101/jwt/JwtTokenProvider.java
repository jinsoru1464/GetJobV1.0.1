package com.example.GetJobV101.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private String secretKey = "yourSecretKey"; // 비밀 키 설정
    private long validityInMilliseconds = 7 * 24 * 60 * 60 * 1000; // 1주일 (7일 * 24시간 * 60분 * 60초 * 1000밀리초)

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // 역할 정보 추가
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}


