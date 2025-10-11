package com.example.mathquiz.application.port.out;

import com.example.mathquiz.domain.Question;
import java.util.List;
import java.util.Optional;

public interface PersistQuestionPort {
    Question save(Question question);
    Optional<Question> findById(Long id);
    List<Question> findAll();
    void deleteById(Long id);
    Question update(Question question);
}