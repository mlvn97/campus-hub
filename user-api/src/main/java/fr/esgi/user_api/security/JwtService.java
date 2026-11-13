package fr.esgi.user_api.security;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

@Service 
public class JwtService {
    
    @Value ("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())                                   // user ID as subject
                .claim("role", role)                                          // user role
                .issuedAt(new Date())                                         // iat (created at)
                .expiration(new Date(System.currentTimeMillis() + 86400000))  // exp (expiration after 24 hours)
                .signWith(getSigningKey())
                .compact();
    }

}
