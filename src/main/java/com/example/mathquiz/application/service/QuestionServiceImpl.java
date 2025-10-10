package com.example.mathquiz.application.service;

import com.example.mathquiz.application.port.in.QuestionService;
import com.example.mathquiz.application.port.out.LoadQuestionsPort;
import com.example.mathquiz.application.port.out.PersistQuestionPort;
import com.example.mathquiz.domain.Question;
import com.example.mathquiz.domain.exception.QuestionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final PersistQuestionPort persistQuestionPort;
    private final LoadQuestionsPort loadQuestionsPort;

    @PostConstruct
    public void init() {
        // Only load questions if database is empty
        if (persistQuestionPort.findAll().isEmpty()) {
            loadQuestionsPort.loadQuestions().forEach(persistQuestionPort::save);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> getRandomQuestions(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1");
        }
        List<Question> allQuestions = persistQuestionPort.findAll();
        if (allQuestions.isEmpty()) {
            return List.of();
        }
        List<Question> mutableList = new ArrayList<>(allQuestions);
        Collections.shuffle(mutableList);

        return mutableList.subList(0, Math.min(count, mutableList.size()));
    }

    @Override
    public Question createQuestion(Question question) {
        return persistQuestionPort.save(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> getQuestion(Long id) {
        return persistQuestionPort.findById(id);
    }

    @Override
    public Question updateQuestion(Long id, Question question) {
        // Verify the question exists before updating
        if (persistQuestionPort.findById(id).isEmpty()) {
            throw new QuestionNotFoundException(String.format("Question with id %s not found.", id));
        }
        question.setId(id);
        return persistQuestionPort.update(question);
    }

    @Override
    public void deleteQuestion(Long id) {
        persistQuestionPort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return persistQuestionPort.findAll();
    }
}
