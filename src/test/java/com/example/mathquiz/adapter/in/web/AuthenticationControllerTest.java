package com.example.mathquiz.adapter.in.web;

import com.example.mathquiz.config.security.JwtTokenUtil;
import com.example.mathquiz.domain.AuthRequest;
import com.example.mathquiz.domain.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @Mock
    private UserDetailsService userDetailsService;
    @InjectMocks
    private AuthenticationController authenticationController;
    private final String testUsername = "testUser";
    private final String testPassword = "testPass";
    private final String validJwt = "valid.jwt.token";
    private AuthRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AuthRequest(testUsername, testPassword);
    }

    @Test
    void login_WithValidCredentials_ReturnsJwtToken() {
        // GIVEN
        UserDetails userDetails = new User(testUsername, testPassword, Collections.emptyList());
        when(userDetailsService.loadUserByUsername(testUsername)).thenReturn(userDetails);
        //when(jwtTokenUtil.generateToken(userDetails)).thenReturn(validJwt);
        // WHEN
        ResponseEntity<AuthResponse> response = authenticationController.createAuthenticationToken(validRequest);
        // THEN
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getJwt()).isEqualTo(validJwt);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername(testUsername);
        //verify(jwtTokenUtil).generateToken(userDetails);
    }

    @Test
    void login_WithInvalidCredentials_ThrowsBadCredentialsException() {
        // GIVEN
        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        // WHEN / THEN
        assertThatThrownBy(() -> authenticationController.createAuthenticationToken(validRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Incorrect username or password");

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void login_WithValidAuthButMissingUser_ThrowsUsernameNotFoundException() {
        // GIVEN
        when(userDetailsService.loadUserByUsername(testUsername))
                .thenThrow(new UsernameNotFoundException("User not found"));
        // WHEN / THEN
        assertThatThrownBy(() -> authenticationController.createAuthenticationToken(validRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void login_VerifyAuthenticationManagerParameters() {
        // GIVEN
        authenticationController.createAuthenticationToken(validRequest);
        // THEN
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(testUsername, testPassword));
    }

    @Test
    void login_VerifyTokenGenerationWithCorrectUserDetails() {
        // GIVEN
        UserDetails mockUserDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        when(userDetailsService.loadUserByUsername(testUsername)).thenReturn(mockUserDetails);
        when(jwtTokenUtil.generateToken(authentication)).thenReturn(validJwt);
        // WHEN
        ResponseEntity<AuthResponse> response = authenticationController.createAuthenticationToken(validRequest);
        // THEN
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getJwt()).isEqualTo(validJwt);
        verify(jwtTokenUtil).generateToken(authentication);
    }
}