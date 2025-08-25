package com.example.mathquiz.adapter.in.web;

import com.example.mathquiz.application.port.in.QuestionService;
import com.example.mathquiz.config.TestSecurityConfig;
import com.example.mathquiz.domain.Question;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@Import(TestSecurityConfig.class)
class QuestionControllerTempTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private QuestionService questionService;

    private final Question sampleQuestion = new Question("What is 2 + 2?", List.of("3", "4", "5", "6"), 1);
    private final String questionJson = """
            {
              "content": "What is 2+2?",
              "options": ["3", "4", "5"],
              "correctOption": 1,
              "category": "Arithmetic"
            }
        """;

    @Test
    @WithMockUser(roles = "USER")
    void getRandomQuestions_shouldReturnValidResponse() throws Exception {
        given(questionService.getRandomQuestions(5)).willReturn(List.of(sampleQuestion));
        mockMvc.perform(get("/questions/random?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].content").value("What is 2 + 2?"))
                .andExpect(jsonPath("$.questions[0].correctOption").value(1));

        verify(questionService).getRandomQuestions(5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuestion_shouldReturnCreatedQuestion() throws Exception {
        given(questionService.createQuestion(any(Question.class))).willReturn(sampleQuestion);
        mockMvc.perform(post("/questions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isCreated());
    }

    @Test
    void createQuestion_withoutAuthorization_shouldForbid() throws Exception {
        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getQuestion_shouldReturnQuestionWhenExists() throws Exception {
        given(questionService.getQuestion(1L)).willReturn(Optional.of(sampleQuestion));
        mockMvc.perform(get("/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctOption").value(1));

        verify(questionService).getQuestion(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getQuestion_shouldReturnNotFoundWhenMissing() throws Exception {
        given(questionService.getQuestion(999L)).willReturn(Optional.empty());
        mockMvc.perform(get("/questions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuestion_shouldReturnUpdatedQuestion() throws Exception {
        Question updatedQuestion = new Question("Updated question", List.of("3", "4", "5", "6"), 1);
        given(questionService.updateQuestion(eq(1L), any(Question.class))).willReturn(updatedQuestion);
        mockMvc.perform(patch("/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "content": "What is 2 + 2?",
                              "options": ["3", "4", "5"],
                              "correctOption": 1
                            }
                            """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated question"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuestion_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/questions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(questionService).deleteQuestion(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllQuestions_shouldReturnAllQuestions() throws Exception {
        given(questionService.getAllQuestions()).willReturn(List.of(sampleQuestion));
        mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questions[0].content").value("What is 2 + 2?"))
                .andExpect(jsonPath("$[0].questions[0].correctOption").value(1));

        verify(questionService).getAllQuestions();
    }
}
