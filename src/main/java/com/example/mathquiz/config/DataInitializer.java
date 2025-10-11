package com.example.mathquiz.config;

import com.example.mathquiz.domain.User;
import com.example.mathquiz.domain.UserRole;
import com.example.mathquiz.repository.UserRepository;
import com.example.mathquiz.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${security.user.password}")
    private String credential;

    @Override
    public void run(String... args) {
        // Create roles if they don't exist
        UserRole adminRole = userRoleRepository.findByName("ADMIN")
                .orElseGet(() -> userRoleRepository.save(new UserRole("ADMIN")));

        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseGet(() -> userRoleRepository.save(new UserRole("USER")));

        // Create admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(credential));
            admin.setRoles(List.of(adminRole, userRole));
            userRepository.save(admin);
        }

        // Create regular user
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode(credential));
            user.setRoles(List.of(userRole));
            userRepository.save(user);
        }
    }
}