package com.example.GetJobV101.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key secretKey; // 비밀 키 설정
    private long validityInMilliseconds = 7 * 24 * 60 * 60 * 1000; // 1주일 (7일 * 24시간 * 60분 * 60초 * 1000밀리초)

    // secretKey를 외부에서 주입받습니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)); // 비밀 키로 안전하게 처리
    }

    // 임시 토큰 생성 메서드
    public String generateTemporaryToken() {
        String temporaryUserId = "temporary";  // 임시 유저 ID
        String role = "ROLE_TEMPORARY";  // 임시 역할

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds); // 임시 토큰의 유효 시간

        return Jwts.builder()
                .setSubject(temporaryUserId)  // 임시 유저 ID 사용
                .claim("role", role)  // 임시 역할 추가
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256) // 생성된 비밀 키로 서명
                .compact();
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)  // 역할 정보 추가
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256) // 이 서명 방식은 JwtUtil에서 사용해야 할 서명 방식과 동일해야 합니다.
                .compact();
    }

}
