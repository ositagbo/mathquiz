package com.example.mathquiz.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class    Question {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    @NotEmpty(message = "Options cannot be empty")
    private List<String> options;

    @NotNull(message = "Correct option is required")
    @Min(value = 0, message = "Correct option must be a positive number")
    private Integer correctOption;

    public Question(String content, List<String> options, int correctOption) {
        this.content = content;
        this.options = options;
        this.correctOption = correctOption;
    }
}