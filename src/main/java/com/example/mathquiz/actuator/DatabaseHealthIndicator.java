package com.example.mathquiz.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final String DATABASE_SERVICE = "DatabaseService";

    @Override
    public Health health() {
        if (isDatabaseHealthy()) {
            return Health.up().withDetail(DATABASE_SERVICE, "Database is running").build();
        }
        return Health.down().withDetail(DATABASE_SERVICE, "Database is not available").build();
    }

    private boolean isDatabaseHealthy() {
        // Add your database health check logic here
        // For simplicity, we'll assume it's always healthy in this example
        return true;
    }
}
