package com.example.mathquiz.adapter.in.web.dto;

import com.example.mathquiz.domain.Question;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestionsResponse {
    private final List<Question> questions;
    private final Summary summary;

    public QuestionsResponse(List<Question> questions) {
        this.questions = questions;
        this.summary = new Summary(questions.size());
    }

    @Getter
    public static class Summary {
        @JsonProperty("totalQuestions")
        private final int totalQuestions;

        @JsonProperty("message")
        private final String message;

        public Summary(int totalQuestions) {
            this.totalQuestions = totalQuestions;
            this.message = generateMessage(totalQuestions);
        }

        private String generateMessage(int count) {
            return switch (count) {
                case 0 -> "No questions available";
                case 1 -> "1 question returned";
                default -> count + " questions returned";
            };
        }
    }
}