package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.PracticeRepository;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-04: Submit Quiz
 * 
 * Verifies that a student can finish a practice session 
 * and that it is correctly marked as closed in the database.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class SubmitQuizIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    private User testStudent;
    private Practice testPractice;

    @BeforeEach
    void setUp() {
        // Setup User
        testStudent = new User("submittester", "submit@quizspark.com");
        testStudent = userRepository.save(testStudent);

        // Security Setup
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testStudent, null, testStudent.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Setup Practice
        testPractice = new Practice();
        testPractice.setUser(testStudent);
        testPractice.setBankNames(List.of("General Knowledge"));
        testPractice.setQuestions(new ArrayList<>());
        testPractice.setClosed(false);
        testPractice = practiceRepository.save(testPractice);
    }

    @Test
    void testFinishPracticeMarksAsClosed() throws Exception {
        String practiceId = testPractice.getId().toString();

        mockMvc.perform(post("/api/v1/practice/id/{practiceId}/finish", practiceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closed").value(true));

        // Verify Database
        Practice updatedPractice = practiceRepository.findById(testPractice.getId()).orElseThrow();
        assertTrue(updatedPractice.isClosed(), "Practice session should be closed after submission");
    }
}
