package com.example.mathquiz.adapter.out.persistence;

import com.example.mathquiz.domain.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "options", source = "options")
    @Mapping(target = "correctOption", source = "correctOption")
    QuestionEntity toEntity(Question question);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "options", source = "options")
    @Mapping(target = "correctOption", source = "correctOption")
    Question toDomain(QuestionEntity entity);
}
