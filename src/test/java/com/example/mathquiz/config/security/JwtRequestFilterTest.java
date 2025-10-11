package com.example.mathquiz.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;
    private final UserDetails userDetails = new User("user", "pass", Collections.emptyList());

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilterAuthLoginPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/login");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldHandleMissingAuthorizationHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldHandleMalformedAuthorizationHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldThrowExceptionForExpiredToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer expiredToken");
        when(jwtTokenUtil.getUsernameFromToken("expiredToken")).thenThrow(new BadCredentialsException("Expired token"));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldSetAuthenticationForValidToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtTokenUtil.getUsernameFromToken("validToken")).thenReturn("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken("validToken", userDetails)).thenReturn(true);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull()
                .extracting("principal")
                .isEqualTo(userDetails);
    }

    @Test
    void shouldNotOverrideExistingAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList()));

        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .extracting("principal")
                .isEqualTo("user");
    }

    @Test
    void shouldHandleInvalidTokenValidation() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtTokenUtil.getUsernameFromToken("invalidToken")).thenReturn("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken("invalidToken", userDetails)).thenReturn(false);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotFilter_WhenRequestUriIsLoginPath() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        boolean result = jwtRequestFilter.shouldNotFilter(request);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideFilterTestCases")
    void shouldNotFilter_WhenRequestUriMatchesVariousScenarios(String requestUri, boolean expectedResult) {
        // Given
        when(request.getRequestURI()).thenReturn(requestUri);
        // When
        boolean result = jwtRequestFilter.shouldNotFilter(request);
        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> provideFilterTestCases() {
        return Stream.of(
                Arguments.of("/auth/login", true),     // Exact match
                Arguments.of("/api/resource", false),  // Different path
                Arguments.of(null, false),             // Null URI
                Arguments.of("/auth/login?redirect=home", false),  // With query params
                Arguments.of("/auth/login/", false),   // Trailing slash
                Arguments.of("", false)                // Empty string
        );
    }
}
