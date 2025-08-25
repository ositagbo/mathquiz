package com.example.mathquiz.adapter.out.persistence;

import com.example.mathquiz.application.port.out.PersistQuestionPort;
import com.example.mathquiz.domain.Question;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaQuestionRepositoryAdapter implements PersistQuestionPort {
    private final QuestionRepository repository;
    private final QuestionMapper mapper;

    public JpaQuestionRepositoryAdapter(QuestionRepository repository, QuestionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Question save(Question question) {
        QuestionEntity entity = mapper.toEntity(question);
        QuestionEntity savedEntity = repository.save(entity);
        // Fetch the saved entity with options to avoid LazyInitializationException
        return mapper.toDomain(repository.findByIdWithOptions(savedEntity.getId())
                .orElse(savedEntity));
    }

    @Override
    public Optional<Question> findById(Long id) {
        return repository.findByIdWithOptions(id).map(mapper::toDomain);
    }

    @Override
    public List<Question> findAll() {
        return repository.findAllWithOptions().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Question update(Question question) {
        QuestionEntity entity = mapper.toEntity(question);
        QuestionEntity updatedEntity = repository.save(entity);
        // Fetch the updated entity with options
        return mapper.toDomain(repository.findByIdWithOptions(updatedEntity.getId())
                .orElse(updatedEntity));
    }
}