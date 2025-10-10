package com.example.mathquiz.integration;

import com.example.mathquiz.config.TestSecurityConfig;
import com.example.mathquiz.config.security.JwtTokenUtil;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    protected JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected String getAdminToken() {
        return "Bearer " +
                jwtTokenUtil.generateToken(new UsernamePasswordAuthenticationToken("admin", "password"));
    }

    protected String getUserToken() {
        return "Bearer " +
                jwtTokenUtil.generateToken(new UsernamePasswordAuthenticationToken("user", "password"));
    }

    protected String getInvalidToken() {
        return "Bearer invalid.token.here";
    }
}