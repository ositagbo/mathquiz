package com.example.mathquiz.adapter.out.persistence;

import com.example.mathquiz.domain.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QuestionMapperTest {
    private QuestionMapper questionMapper;

    @BeforeEach
    void setUp() {
        questionMapper = Mappers.getMapper(QuestionMapper.class);
    }

    @Test
    void toEntity_shouldMapDomainToEntity() {
        // Given
        Question question = new Question("Test question", List.of("A", "B", "C", "D"), 2);
        question.setId(1L);
        // When
        QuestionEntity entity = questionMapper.toEntity(question);
        // Then
        assertThat(entity)
                .isNotNull()
                .extracting(
                        QuestionEntity::getId,
                        QuestionEntity::getContent,
                        QuestionEntity::getCorrectOption
                )
                .containsExactly(1L, "Test question", 2);

        assertThat(entity.getOptions())
                .isNotNull()
                .hasSize(4)
                .containsExactly("A", "B", "C", "D");
    }

    @Test
    void toEntity_withNullDomain_shouldReturnNull() {
        // When
        QuestionEntity entity = questionMapper.toEntity(null);
        // Then
        assertThat(entity).isNull();
    }

    @Test
    void toDomain_shouldMapEntityToDomain() {
        // Given
        QuestionEntity entity = new QuestionEntity("Test question", List.of("A", "B", "C", "D"), 2);
        entity.setId(1L);
        // When
        Question domain = questionMapper.toDomain(entity);

        // Then
        assertThat(domain)
                .isNotNull()
                .extracting(
                        Question::getId,
                        Question::getContent,
                        Question::getCorrectOption
                )
                .containsExactly(1L, "Test question", 2);

        assertThat(domain.getOptions())
                .isNotNull()
                .hasSize(4)
                .containsExactly("A", "B", "C", "D");
    }

    @Test
    void toDomain_withNullEntity_shouldReturnNull() {
        // When
        Question domain = questionMapper.toDomain(null);
        // Then
        assertThat(domain).isNull();
    }

    @Test
    void toEntity_andToDomain_shouldBeReversible() {
        // Given
        Question originalQuestion = new Question("Reversible test", List.of("X", "Y", "Z"), 1);
        originalQuestion.setId(5L);
        // When
        QuestionEntity entity = questionMapper.toEntity(originalQuestion);
        Question convertedQuestion = questionMapper.toDomain(entity);
        // Then
        assertThat(convertedQuestion)
                .isNotNull()
                .isEqualTo(originalQuestion);

        assertThat(convertedQuestion.getId()).isEqualTo(originalQuestion.getId());
        assertThat(convertedQuestion.getContent()).isEqualTo(originalQuestion.getContent());
        assertThat(convertedQuestion.getOptions()).isEqualTo(originalQuestion.getOptions());
        assertThat(convertedQuestion.getCorrectOption()).isEqualTo(originalQuestion.getCorrectOption());
    }

    @Test
    void toEntity_shouldHandleEmptyOptions() {
        // Given
        Question question = new Question("Empty options", List.of(), 0);
        question.setId(2L);
        // When
        QuestionEntity entity = questionMapper.toEntity(question);
        // Then
        assertThat(entity)
                .isNotNull()
                .extracting(QuestionEntity::getOptions)
                .isEqualTo(List.of());
    }

    @Test
    void toDomain_shouldHandleEmptyOptions() {
        // Given
        QuestionEntity entity = new QuestionEntity("Empty options", List.of(), 0);
        entity.setId(2L);
        // When
        Question domain = questionMapper.toDomain(entity);
        // Then
        assertThat(domain)
                .isNotNull()
                .extracting(Question::getOptions)
                .isEqualTo(List.of());
    }
}