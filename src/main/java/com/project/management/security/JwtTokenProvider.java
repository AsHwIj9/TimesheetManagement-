package com.project.management.security;

import com.project.management.Models.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKey secretKey; // Injected from JwtConfig

    @Value("${jwt.expiration-in-ms:86400000}")  // Default 24 hours
    private long expirationInMs;

    // Generate a JWT Token
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .setSubject(user.getId())                     // Set user ID as subject
                .setIssuedAt(now)                             // Issued at
                .setExpiration(expiryDate)                    // Expiration date
                .claim("username", user.getUsername())        // Custom claims
                .claim("role", user.getRole())                // Custom claims
                .signWith(secretKey, SignatureAlgorithm.HS256)  // Use the injected secret key
                .compact();
    }

    // Validate the JWT Token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Use the injected secret key
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log or handle invalid token cases if needed
            return false;
        }
    }

    // Extract User ID from JWT Token
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey) // Use the injected secret key
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();  // Return the user ID
    }
}
