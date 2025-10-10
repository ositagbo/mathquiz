package com.example.mathquiz.adapter.in.web;

import com.example.mathquiz.adapter.in.web.exception.ErrorResponse;
import com.example.mathquiz.adapter.in.web.exception.UnauthorizedException;
import com.example.mathquiz.domain.exception.QuestionNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler exceptionHandler;
    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private HttpServletResponse httpServletResponse;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleQuestionNotFoundException_ReturnsNotFoundMessage() {
        // Given
        String errorMessage = "Question not found";
        QuestionNotFoundException ex = new QuestionNotFoundException(errorMessage);
        // When
        String result = exceptionHandler.handleNotFound(ex);
        // Then
        assertThat(result).isEqualTo(errorMessage);
    }

    @Test
    void handleMethodArgumentNotValidException_ReturnsFieldErrors() {
        // Given
        FieldError fieldError1 = new FieldError("object", "field1", "must not be null");
        FieldError fieldError2 = new FieldError("object", "field2", "must be positive");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(methodArgumentNotValidException);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("field1", "must not be null")
                .containsEntry("field2", "must be positive");
    }

    @Test
    void handleBadCredentialsException_ReturnsUnauthorizedResponse() {
        // Given
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentialsException(ex);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid credentials")
                .containsEntry("message", "Incorrect username or password.");
    }

    @Test
    void handleGenericException_ReturnsInternalServerErrorResponse() {
        // Given
        Exception ex = new Exception("Unexpected error occurred");
        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(ex);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .containsEntry("error", "Internal server error")
                .containsEntry("message", "Unexpected error occurred");
    }

    @Test
    void handleAccessDeniedException_ReturnsForbiddenResponse() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDeniedException(ex);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
                .containsEntry("error", "Access Denied")
                .containsEntry("message", "Access denied");
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequestResponse() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgumentException(ex);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid argument")
                .containsEntry("message", "Invalid argument");
    }

    @Test
    void handleUnauthorizedException_ReturnsUnauthorizedErrorResponse() {
        // Given
        UnauthorizedException ex = new UnauthorizedException("Token expired");
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorizedException(ex, httpServletResponse);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .extracting(
                        ErrorResponse::getStatus,
                        ErrorResponse::getError,
                        ErrorResponse::getMessage
                )
                .containsExactly(
                        HttpStatus.UNAUTHORIZED.value(),
                        "AUTH_ERROR",
                        "Token expired"
                );
    }
}