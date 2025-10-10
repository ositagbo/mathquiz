package com.example.mathquiz.domain;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record JwtResponse(
        String username,
        Collection<? extends GrantedAuthority> authorities,
        String jwtToken
) {}
