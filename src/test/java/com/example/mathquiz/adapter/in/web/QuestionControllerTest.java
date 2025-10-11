package com.example.mathquiz.adapter.in.web;

import com.example.mathquiz.application.port.in.QuestionService;
import com.example.mathquiz.config.TestSecurityConfig;
import com.example.mathquiz.domain.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@Import(TestSecurityConfig.class)
class QuestionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private QuestionService questionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = "USER")
    void getRandomQuestions_shouldReturnRandomQuestions() throws Exception {
        // Given
        List<Question> questions = List.of(
                new Question("What is 2 + 2?", List.of("3", "4", "5", "6"), 1),
                new Question("What is 5 - 3?", List.of("1", "2", "3", "4"), 1)
        );
        when(questionService.getRandomQuestions(5)).thenReturn(questions);
        // When/Then
        mockMvc.perform(get("/questions/random?count=5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(jsonPath("$.questions[0].content").value("What is 2 + 2?"))
                .andExpect(jsonPath("$.questions[1].content").value("What is 5 - 3?"));

        verify(questionService).getRandomQuestions(5);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRandomQuestions_withDefaultCount_shouldUseDefaultValue() throws Exception {
        // Given
        when(questionService.getRandomQuestions(5)).thenReturn(List.of());
        // When/Then
        mockMvc.perform(get("/questions/random")).andExpect(status().isOk());

        verify(questionService).getRandomQuestions(5);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRandomQuestions_withNegativeCount_shouldThrowException() throws Exception {
        // Given
        when(questionService.getRandomQuestions(-1)).thenThrow(new IllegalArgumentException());
        // When/Then
        mockMvc.perform(get("/questions/random?count=-1")).andExpect(status().isBadRequest());

        verify(questionService).getRandomQuestions(-1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllQuestions_shouldUseDefaultValue() throws Exception {
        // Given
        when(questionService.getAllQuestions()).thenReturn(List.of());
        // When/Then
        mockMvc.perform(get("/questions")).andExpect(status().isOk());

        verify(questionService).getAllQuestions();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuestion_shouldCreateNewQuestion() throws Exception {
        // Given
        Question newQuestion = new Question("New question", List.of("A", "B", "C", "D"), 2);
        Question savedQuestion = new Question("New question", List.of("A", "B", "C", "D"), 2);
        savedQuestion.setId(1L);

        when(questionService.createQuestion(any(Question.class))).thenReturn(savedQuestion);
        // When/Then
        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newQuestion))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("New question"))
                .andExpect(jsonPath("$.correctOption").value(2));

        verify(questionService).createQuestion(any(Question.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getQuestion_shouldReturnQuestionWhenExists() throws Exception {
        // Given
        Question question = new Question("Test question", List.of("A", "B", "C", "D"), 0);
        question.setId(1L);

        when(questionService.getQuestion(1L)).thenReturn(Optional.of(question));
        // When/Then
        mockMvc.perform(get("/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test question"))
                .andExpect(jsonPath("$.correctOption").value(0));

        verify(questionService).getQuestion(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getQuestion_shouldReturnNotFoundWhenQuestionDoesNotExist() throws Exception {
        // Given
        when(questionService.getQuestion(1L)).thenReturn(Optional.empty());
        // When/Then
        mockMvc.perform(get("/questions/1")).andExpect(status().isNotFound());

        verify(questionService).getQuestion(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateQuestion_shouldUpdateExistingQuestion() throws Exception {
        // Given
        Question updatedQuestion = new Question("Updated question", List.of("A", "B", "C", "D"), 3);
        updatedQuestion.setId(1L);

        when(questionService.updateQuestion(eq(1L), any(Question.class))).thenReturn(updatedQuestion);
        // When/Then
        mockMvc.perform(patch("/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedQuestion))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Updated question"))
                .andExpect(jsonPath("$.correctOption").value(3));

        verify(questionService).updateQuestion(eq(1L), any(Question.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteQuestion_shouldDeleteQuestion() throws Exception {
        // Given
        doNothing().when(questionService).deleteQuestion(1L);
        // When/Then
        mockMvc.perform(delete("/questions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(questionService).deleteQuestion(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuestion_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given - Create invalid question with empty content, empty options, and negative correctOption
        Question invalidQuestion = new Question("", List.of(), -1);
        // When/Then
        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidQuestion))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("Content is required"))
                .andExpect(jsonPath("$.options").value("Options cannot be empty"))
                .andExpect(jsonPath("$.correctOption").value("Correct option must be a positive number"));

        verify(questionService, never()).createQuestion(any(Question.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createQuestion_withNullFields_shouldReturnBadRequest() throws Exception {
        // Given - Create a question with null fields
        String invalidJson = "{\"content\":null,\"options\":null,\"correctOption\":null}";
        // When/Then
        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("Content is required"))
                .andExpect(jsonPath("$.options").value("Options cannot be empty"))
                .andExpect(jsonPath("$.correctOption").value("Correct option is required"));

        verify(questionService, never()).createQuestion(any(Question.class));
    }
}