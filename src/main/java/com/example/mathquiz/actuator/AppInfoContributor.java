package com.example.mathquiz.actuator;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class AppInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appDetails = new HashMap<>();
        appDetails.put("name", "Grade 1 Math Questions API");
        appDetails.put("description", "REST API for generating Grade 1 mathematics questions");
        appDetails.put("version", "1.0.0");

        Map<String, Object> authorDetails = new HashMap<>();
        authorDetails.put("name", "Math Quiz Team");
        authorDetails.put("email", "contact@mathquiz.example.com");

        builder.withDetail("application", appDetails).withDetail("author", authorDetails);
    }
}
