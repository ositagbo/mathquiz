package com.example.mathquiz.integration;

import com.example.mathquiz.adapter.out.persistence.QuestionEntity;
import com.example.mathquiz.adapter.out.persistence.QuestionMapper;
import com.example.mathquiz.adapter.out.persistence.QuestionRepository;
import com.example.mathquiz.domain.Question;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@TestConfiguration
public class TestDataInitializer {
    private static final QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Bean
    public TestDataInitializerRunner testDataInitializerRunner(QuestionRepository questionRepository) {
        return new TestDataInitializerRunner(questionRepository);
    }

    public static class TestDataInitializerRunner {
        private final QuestionRepository questionRepository;

        public TestDataInitializerRunner(QuestionRepository questionRepository) {
            this.questionRepository = questionRepository;
            initializeData();
        }

        private void initializeData() {
            // Clear existing data
            questionRepository.deleteAll();

            // Create test questions
            List<QuestionEntity> questions = List.of(
                    INSTANCE.toEntity(new Question("What is 2 + 2?", List.of("3", "4", "5", "6"), 1)),
                    INSTANCE.toEntity(new Question("What is 5 - 3?", List.of("1", "2", "3", "4"), 1)),
                    INSTANCE.toEntity(new Question("What is 3 × 2?", List.of("4", "5", "6", "7"), 2)),
                    INSTANCE.toEntity(new Question("What is 10 ÷ 2?", List.of("2", "5", "8", "10"), 1)),
                    INSTANCE.toEntity(new Question("What is 7 + 3?", List.of("9", "10", "11", "12"), 1))
            );

            // Save questions
            questionRepository.saveAll(questions);
        }
    }
}
