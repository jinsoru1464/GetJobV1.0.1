package com.example.GetJobV101.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 3; // 3시간

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    // ✅ 이 부분 새로 추가!
    public String generateToken(String loginId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getLoginIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String loginId = getLoginIdFromToken(token);
        return loginId.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }




    public void validateTokenOrThrow(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // 여기서 유효하지 않으면 예외 발생함
        } catch (ExpiredJwtException e) {
            throw new JwtException("⚠️ 토큰이 만료되었습니다. 다시 로그인 해주세요.");
        } catch (Exception e) {
            throw new JwtException("🚫 유효하지 않은 토큰입니다.");
        }
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
