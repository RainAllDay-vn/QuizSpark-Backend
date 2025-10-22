package com.quizspark.quizsparkserver.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private SecretKey secretKey;
    // Token validity duration (e.g., 1 day)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    @PostConstruct
    public void init() {
        // Create a secure random key for HS256 signing
        this.secretKey = Jwts.SIG.HS256.key().build();
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String validateToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        Claims claims = jws.getPayload();
        return claims.getSubject();
    }
}
