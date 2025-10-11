package com.example.mathquiz.domain;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record JwtResponse(String username, Collection<? extends GrantedAuthority> authorities, String jwtToken) {}
