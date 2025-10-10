package com.example.mathquiz.integration;

import com.example.mathquiz.adapter.in.web.dto.QuestionsResponse;
import com.example.mathquiz.domain.Question;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class QuestionControllerITest {
    @Autowired
    private TestRestTemplate restTemplate;
    private Question sampleQuestion;
    private String adminToken;
    private String userToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        sampleQuestion = new Question("What is 2 + 2?", List.of("3", "4", "5", "6"), 1);
        adminToken = generateJwtToken("admin", "ROLE_ADMIN");
        userToken = generateJwtToken("user", "ROLE_USER");
        invalidToken = "invalid.token.here";
    }

    // CREATE
    @Test
    void createQuestion_shouldReturn201ForAdmin() {
        HttpEntity<Question> request = createAuthEntity(sampleQuestion, adminToken);
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                request,
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void createQuestion_shouldReturn403ForUser() {
        HttpEntity<Question> request = createAuthEntity(sampleQuestion, userToken);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // READ
    @Test
    void getQuestion_shouldReturn200ForValidToken() {
        Question created = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                createAuthEntity(sampleQuestion, adminToken),
                Question.class
        ).getBody();

        assertThat(created).isNotNull();
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.GET,
                createAuthEntity(null, userToken),
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    void getQuestion_shouldReturn401ForMissingToken() {
        ResponseEntity<Question> response = restTemplate.getForEntity(
                "/questions/1",
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // UPDATE
    @Test
    void updateQuestion_shouldReturn200ForAdmin() {
        Question created = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                createAuthEntity(sampleQuestion, adminToken),
                Question.class
        ).getBody();
        assertThat(created).isNotNull();
        created.setContent("Updated question");

        ResponseEntity<Void> updateResponse = restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.PATCH,
                createAuthEntity(created, adminToken),
                Void.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateQuestion_shouldReturn403ForUser() {
        HttpEntity<Question> request = createAuthEntity(sampleQuestion, userToken);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions/1",
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // DELETE
    @Test
    void deleteQuestion_shouldReturn204ForAdmin() {
        Question created = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                createAuthEntity(sampleQuestion, adminToken),
                Question.class
        ).getBody();
        assertThat(created).isNotNull();
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.DELETE,
                createAuthEntity(null, adminToken),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteQuestion_shouldReturn401ForExpiredToken() {
        HttpEntity<?> request = createAuthEntity(null, invalidToken);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions/1",
                HttpMethod.DELETE,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // SPECIAL ENDPOINTS
    @Test
    void getRandomQuestions_shouldReturn401ForInvalidToken() {
        HttpEntity<?> request = createAuthEntity(null, invalidToken);
        ResponseEntity<List<Question>> response = restTemplate.exchange(
                "/questions/random?count=3",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getRandomQuestions_shouldReturn401ForInvalidTokenNew() {
        HttpEntity<?> request = createAuthEntity(null, "invalid.token");
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions/random?count=3",
                HttpMethod.GET,
                request,
                Void.class
        );

        assertThat(response.getStatusCode())
                .withFailMessage("Expected 401 UNAUTHORIZED but got %s. Response body: %s",
                        response.getStatusCode(),
                        response.getBody())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getAllQuestions_shouldReturn200ForAuthenticatedUsers() {
        restTemplate.exchange("/questions", HttpMethod.POST, createAuthEntity(sampleQuestion, adminToken), Question.class);
        ResponseEntity<List<Question>> response = restTemplate.exchange(
                "/questions",
                HttpMethod.GET,
                createAuthEntity(null, userToken),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void createQuestion_shouldReturnCreatedForAdmin() {
        HttpEntity<Question> request = createAuthEntity(sampleQuestion, adminToken);
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                request,
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createQuestion_shouldForbidForUserRole() {
        HttpEntity<Question> request = createAuthEntity(sampleQuestion, userToken);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getQuestion_shouldAllowAuthenticatedUsers() {
        // First create as admin
        Question created = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                createAuthEntity(sampleQuestion, adminToken),
                Question.class
        ).getBody();

        // Then retrieve as user
        assertThat(created).isNotNull();
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.GET,
                createAuthEntity(null, userToken),
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getQuestion_shouldDenyUnauthenticatedAccess() {
        ResponseEntity<Question> response = restTemplate.getForEntity(
                "/questions/1",
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateQuestion_shouldValidateTokenIntegrity() {
        HttpEntity<Question> request = createAuthEntity(sampleQuestion, invalidToken);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions/1",
                HttpMethod.PATCH,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuestion_shouldReturnCreatedQuestion() {
        ResponseEntity<Question> response = restTemplate.postForEntity(
                "/questions",
                createAuthEntity(sampleQuestion, adminToken),
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("What is 2 + 2?");
    }

    @Test
    void getQuestion_shouldReturnQuestionWhenExists() {
        // Create question with proper admin auth
        Question created = restTemplate.exchange(
                "/questions",
                HttpMethod.POST,
                createAuthEntity(sampleQuestion, adminToken),
                Question.class
        ).getBody();

        assertThat(created).isNotNull();
        // Get request with proper auth headers
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.GET,
                createAuthEntity(null, userToken),
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getQuestion_shouldReturnNotFoundWhenMissing() {
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions/9999",
                HttpMethod.GET,
                createAuthEntity(null, userToken),
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuestion_shouldModifyExistingQuestion() {
        Question created = restTemplate.postForObject("/questions",
                createAuthEntity(sampleQuestion, adminToken),
                Question.class);
        created.setContent("Updated question");
        restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.PATCH,
                createAuthEntity(created, adminToken),
                Void.class
        );
        // Get request with proper auth headers
        ResponseEntity<Question> response = restTemplate.exchange(
                "/questions/" + created.getId(),
                HttpMethod.GET,
                createAuthEntity(null, userToken),
                Question.class
        );
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("Updated question");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRandomQuestions_shouldReturnRequestedCount() {
        for (int i = 0; i < 5; i++) {
            restTemplate.postForObject("/questions", sampleQuestion, Question.class);
        }
        var response = restTemplate.getForObject(
                "/questions/random?count=3",
                QuestionsResponse.class
        );

        assertThat(response.getQuestions()).hasSize(3);
    }

    @Test
    void getRandomQuestions_shouldValidateCountParameter() {
        var response = restTemplate.getForEntity(
                "/questions/random?count=0",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllQuestions_shouldReturnAllCreatedQuestions() {
        restTemplate.postForObject("/questions", createAuthEntity(sampleQuestion, userToken), Question.class);
        restTemplate.postForObject("/questions", createAuthEntity(sampleQuestion, userToken), Question.class);
        var response = restTemplate.getForEntity("/questions",
                List.class, createAuthEntity(sampleQuestion, userToken));

        assertThat(response).isNotNull();
    }

    // Security tests
    @Test
    void createQuestion_shouldDenyUnauthorizedAccess() {
        ResponseEntity<Question> response = restTemplate.postForEntity(
                "/questions",
                sampleQuestion,
                Question.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteQuestion_shouldDenyNonAdminAccess() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/questions/1",
                HttpMethod.DELETE,
                createAuthEntity(null, userToken),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode("dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHk=");
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT secret key configuration", e);
        }
    }

    private String generateJwtToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(getSigningKey())
                .compact();
    }

    private <T> HttpEntity<T> createAuthEntity(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
