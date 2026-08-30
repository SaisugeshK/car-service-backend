package com.example.InventoryManagementSystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Pre-deployment fix — this used to be a hardcoded literal here, completely ignoring the
    // jwt.secret/jwt.expiration properties in application.properties (which looked like they
    // controlled this but did nothing). That meant every environment — dev, staging, prod —
    // signed tokens with the exact same key committed in source, and JWT_SECRET set at deploy
    // time had no effect at all. Now actually wired to the property, so JWT_SECRET truly governs
    // the signing key in production, with the same value as before kept as the fallback so local
    // dev behavior is unchanged when no env var is set.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String roleName) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", roleName)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Parses and validates signature + expiry in one shot; returns null on any
    // failure (expired, malformed, bad signature) so callers can treat the
    // request as unauthenticated instead of blowing up mid-filter-chain.
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        return parseClaims(token) != null;
    }

    public String extractEmail(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extractRole(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }
}
