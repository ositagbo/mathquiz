package com.example.mathquiz.application.port.in;

import com.example.mathquiz.domain.Question;
import java.util.List;
import java.util.Optional;

public interface QuestionService {
    List<Question> getRandomQuestions(int count);
    Question createQuestion(Question question);
    Optional<Question> getQuestion(Long id);
    Question updateQuestion(Long id, Question question);
    void deleteQuestion(Long id);
    List<Question> getAllQuestions();
}