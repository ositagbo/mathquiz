package com.example.mathquiz.adapter.out.json;

import com.example.mathquiz.application.port.out.LoadQuestionsPort;
import com.example.mathquiz.domain.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JsonQuestionLoader implements LoadQuestionsPort {
    @Override
    public List<Question> loadQuestions() {
        try {
            ClassPathResource resource = new ClassPathResource("sample-questions.json");
            ObjectMapper mapper = new ObjectMapper();
            return List.of(mapper.readValue(resource.getInputStream(), Question[].class));
        } catch (IOException _) {
            return List.of();
        }
    }
}
