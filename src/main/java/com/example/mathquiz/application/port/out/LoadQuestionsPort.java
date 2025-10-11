package com.example.mathquiz.application.port.out;

import com.example.mathquiz.domain.Question;
import java.util.List;

public interface LoadQuestionsPort {
    List<Question> loadQuestions();
}