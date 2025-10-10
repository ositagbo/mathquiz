package com.example.mathquiz.application.service;

import com.example.mathquiz.application.port.out.LoadQuestionsPort;
import com.example.mathquiz.application.port.out.PersistQuestionPort;
import com.example.mathquiz.domain.Question;
import com.example.mathquiz.domain.exception.QuestionNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {
    @Mock private PersistQuestionPort persistQuestionPort;
    @Mock private LoadQuestionsPort loadQuestionsPort;
    @InjectMocks private QuestionServiceImpl questionService;

    @Test
    void getRandomQuestions_shouldReturnShuffledQuestions() {
        // Given
        List<Question> questions = List.of(
                new Question("Q1", List.of("A", "B"), 0),
                new Question("Q2", List.of("C", "D"), 1)
        );

        // Use an immutable list to simulate Hibernate's behavior
        List<Question> immutableList = Collections.unmodifiableList(questions);
        when(persistQuestionPort.findAll()).thenReturn(immutableList);
        // When
        List<Question> result = questionService.getRandomQuestions(2);
        // Then
        assertThat(result).hasSize(2).containsExactlyInAnyOrderElementsOf(questions);
    }

    @Test
    void getRandomQuestions_withEmptyDatabase_shouldReturnEmptyList() {
        // Given
        when(persistQuestionPort.findAll()).thenReturn(Collections.emptyList());
        // When
        List<Question> result = questionService.getRandomQuestions(5);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createQuestion_shouldPersistQuestion() {
        // Given
        Question question = new Question("New", List.of("A", "B"), 0);
        when(persistQuestionPort.save(any())).thenReturn(question);
        // When
        Question result = questionService.createQuestion(question);
        // Then
        assertThat(result).isEqualTo(question);
        verify(persistQuestionPort).save(question);
    }

    @Test
    void getQuestion_shouldReturnQuestionWhenExists() {
        // Given
        Question question = new Question("Test", List.of("A", "B"), 0);
        question.setId(1L);
        when(persistQuestionPort.findById(1L)).thenReturn(Optional.of(question));
        // When
        Optional<Question> result = questionService.getQuestion(1L);
        // Then
        assertThat(result).isPresent().contains(question);
    }

    @Test
    void getQuestion_shouldReturnEmptyWhenNotExists() {
        // Given
        when(persistQuestionPort.findById(1L)).thenReturn(Optional.empty());
        // When
        Optional<Question> result = questionService.getQuestion(1L);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void updateQuestion_shouldUpdateExistingQuestion() {
        // Given
        Question existing = new Question("Old", List.of("A", "B"), 0);
        existing.setId(1L);
        Question updated = new Question("New", List.of("C", "D"), 1);

        when(persistQuestionPort.findById(1L)).thenReturn(Optional.of(existing));
        when(persistQuestionPort.update(any())).thenReturn(updated);
        // When
        Question result = questionService.updateQuestion(1L, updated);
        // Then
        assertThat(result).isEqualTo(updated);
        verify(persistQuestionPort).update(updated);
    }

    @Test
    void updateQuestion_shouldThrowExceptionWhenQuestionNotFound() {
        // Given
        Question question = new Question("Test", List.of("A", "B"), 0);
        when(persistQuestionPort.findById(1L)).thenReturn(Optional.empty());
        // When/Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, question))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("Question with id 1 not found");
    }

    @Test
    void deleteQuestion_shouldCallDeleteOnPort() {
        // When
        questionService.deleteQuestion(1L);
        // Then
        verify(persistQuestionPort).deleteById(1L);
        String value = " ";
        assertThat(StringUtils.isBlank(value)).isTrue();
    }
}