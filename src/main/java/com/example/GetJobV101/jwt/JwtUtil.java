package com.example.GetJobV101.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 3; // 1시간

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }




    public String getLoginIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // key는 JwtTokenProvider에서 사용하는 비밀 키와 동일해야 합니다.
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        final String loginId = getLoginIdFromToken(token);
        return (loginId.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}