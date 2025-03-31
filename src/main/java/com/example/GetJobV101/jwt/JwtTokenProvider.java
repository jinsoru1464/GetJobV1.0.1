package com.example.GetJobV101.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private Key secretKey; // 비밀 키 설정
    private long validityInMilliseconds = 7 * 24 * 60 * 60 * 1000; // 1주일 (7일 * 24시간 * 60분 * 60초 * 1000밀리초)


    public JwtTokenProvider() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);  // 보안적으로 안전한 키 생성
    }

    // 기존의 토큰 생성 메서드 (사용자 인증용)
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

    // 임시 토큰 생성 메서드 추가
    public String generateTemporaryToken() {
        String temporaryUserId = "temporary";  // 임시 유저 ID
        String role = "ROLE_TEMPORARY";  // 임시 역할 (선택적, 필요시 추가)

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds); // 임시 토큰의 유효 시간은 1주일로 설정

        return Jwts.builder()
                .setSubject(temporaryUserId)  // 임시 유저 ID 사용
                .claim("role", role)  // 임시 역할 추가
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}



