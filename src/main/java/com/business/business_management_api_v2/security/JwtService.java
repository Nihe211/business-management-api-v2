package com.business.business_management_api_v2.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    @Getter
    private final long accessExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs){
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generateAccessToken(CustomUserDetails userDetails){
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId",userDetails.getId())
                .claim("role",userDetails.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime()+accessExpirationMs))
                .signWith(key)
                .compact();
    }
    /** Ném JwtException nếu chữ ký sai hoặc token hết hạn. */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
