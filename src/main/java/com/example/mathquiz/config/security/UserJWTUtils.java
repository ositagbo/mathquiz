package com.example.mathquiz.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
public class UserJWTUtils {
    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    @Value("${jwt.secret}")
    private String secret;

    public UserJWTUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        //this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.secretKey = Jwts.SIG.HS512.key().build();
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public boolean validateJwtToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            Claims claims = jws.getPayload();

            return isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException _) {
            return false;
        }
    }

    public UserDetails getUserPrincipalFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<GrantedAuthority> authorities = ((List<?>) claims.get("roles"))
                .stream()
                .map(role -> new SimpleGrantedAuthority((String) role))
                .collect(Collectors.toList());

        return User.builder()
                .username(claims.getSubject())
                .password("")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (jwtExpirationMs * 1000)))
                .signWith(getSigningKey())
                //.signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    private boolean isTokenExpired(Claims claims) {
        if (isNull(claims.getExpiration())) {
            return false;
        }
        Date now = new Date();
        return claims.getExpiration().after(now) && claims.getIssuedAt().before(now);
    }

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT secret key configuration", e);
        }
    }
}
