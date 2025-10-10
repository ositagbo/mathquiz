package com.example.mathquiz.integration;

import com.example.mathquiz.domain.AuthRequest;
import com.example.mathquiz.domain.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationControllerITest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Use lowercase table names matching entity mappings
        jdbcTemplate.update("DELETE FROM users_roles");
        jdbcTemplate.update("DELETE FROM roles");
        jdbcTemplate.update("DELETE FROM users");

        // Insert with explicit column names
        jdbcTemplate.update(
                "INSERT INTO users (username, password) VALUES (?, ?)",
                "testUser", passwordEncoder.encode("validPass123")
        );
        jdbcTemplate.update("INSERT INTO roles (name) VALUES (?)", "ROLE_USER");
    }

    @Test
    void login_WithValidCredentials_ReturnsJwtToken() {
        AuthRequest request = new AuthRequest("testUser", "validPass123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login",
                request,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getJwt()).isNotBlank();
    }

    @Test
    void debugPassword() {
        String rawPassword = "validPass123";
        String encoded = passwordEncoder.encode(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }


    @Test
    void login_WithValidCredentials_ReturnsJwtTokenNew() {
        // 1. Verify test user exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = 'testUser'",
                Integer.class
        );
        assertThat(count).isEqualTo(1);

        // 2. Execute authentication
        AuthRequest request = new AuthRequest("testUser", "validPass123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login",
                request,
                AuthResponse.class
        );

        // 3. Verify response
        assertThat(response.getStatusCode())
                .as("Should authenticate with valid credentials")
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getJwt())
                .isNotBlank()
                .satisfies(token -> {
                    Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())  // Changed from setSigningKey
                            .build()
                            .parseSignedClaims(token)  // Changed method name
                            .getPayload();

                    assertThat(claims.getSubject()).isEqualTo("testUser");
                });
    }

    @Test
    void login_WithInvalidPassword_ReturnsUnauthorized() {
        AuthRequest request = new AuthRequest("testUser", "wrongPassword");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login",
                request,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_WithNonExistentUser_ReturnsUnauthorized() {
        AuthRequest request = new AuthRequest("nonExistentUser", "anyPassword");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login",
                request,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_WithMissingUsername_ReturnsBadRequest() {
        AuthRequest request = new AuthRequest(null, "validPass123");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("username");
    }

    @Test
    void login_WithMissingPassword_ReturnsBadRequest() {
        AuthRequest request = new AuthRequest("testUser", null);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login",
                request,
                String.class
        );

        assertThat(response.getStatusCode())
                .as("Should return BAD_REQUEST when password is missing")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()).contains("Password is required");
    }

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode("dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHk=");
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT secret key configuration", e);
        }
    }
}
