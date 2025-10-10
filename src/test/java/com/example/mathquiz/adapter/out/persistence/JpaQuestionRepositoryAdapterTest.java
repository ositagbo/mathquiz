package com.example.mathquiz.adapter.out.persistence;

import com.example.mathquiz.domain.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaQuestionRepositoryAdapterTest {
    @Mock
    private QuestionRepository repository;
    @Spy
    private QuestionMapper mapper = Mappers.getMapper(QuestionMapper.class);
    @InjectMocks
    private JpaQuestionRepositoryAdapter adapter;

    @Test
    void save_shouldMapAndSaveEntity() {
        // Given
        Question question = new Question("Test question", List.of("A", "B", "C"), 1);
        QuestionEntity entity = mapper.toEntity(question);
        when(repository.save(any(QuestionEntity.class))).thenReturn(entity);

        // When
        Question result = adapter.save(question);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Test question");
        verify(repository).save(any(QuestionEntity.class));
    }
}