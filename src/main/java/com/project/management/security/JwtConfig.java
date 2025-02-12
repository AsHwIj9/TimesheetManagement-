package com.project.management.security;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class JwtConfig {
    private static final String SECRET_KEY_STRING = "your-very-secure-secret-key-which-should-be-long-enough";
    @Bean
    public SecretKey secretKey() {

         return Keys.hmacShaKeyFor(Base64.getEncoder().encode(SECRET_KEY_STRING.getBytes()));
    }



}
