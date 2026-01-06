package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-02: Join Live Quiz Session
 * 
 * NOTE: This test defines the required behavior for the Join Session feature.
 * Since the QuizSessionController is not yet implemented in the source, 
 * this test is built as a specification for the future implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class JoinSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testStudent;

    @BeforeEach
    void setUp() {
        // Create a test student
        testStudent = new User("student_one", "student@quizspark.com");
        testStudent.setRole("ROLE_STUDENT");
        testStudent = userRepository.save(testStudent);

        // Security Setup
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testStudent, null, testStudent.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testJoinSessionWithValid6DigitCode() throws Exception {
        // Requirement: Entering a valid 6-digit code must allow joining.
        // Input: "123456"
        
        String validCode = "123456";
        String joinRequestJson = String.format("{\"code\": \"%s\"}", validCode);

        // This endpoint is part of the proposed TC-02 requirement.
        // It should return 200 OK and session details like sessionId and status.
        mockMvc.perform(post("/api/v1/quiz/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.status").value("WAITING")); // Or "ACTIVE"
    }

    @Test
    void testJoinSessionWithInvalidCode() throws Exception {
        // Requirement: Invalid codes should be rejected.
        String invalidCode = "000000";
        String joinRequestJson = String.format("{\"code\": \"%s\"}", invalidCode);

        mockMvc.perform(post("/api/v1/quiz/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestJson))
                .andExpect(status().isNotFound());
    }
}
