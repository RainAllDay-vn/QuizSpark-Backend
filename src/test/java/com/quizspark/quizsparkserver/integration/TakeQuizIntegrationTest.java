package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.PracticeRepository;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.QuestionRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-03: Take Quiz
 * 
 * Verifies that a student can submit answers to a practice session 
 * and that the system records the time spent.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class TakeQuizIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private User testStudent;
    private Practice testPractice;

    @BeforeEach
    void setUp() {
        // 1. Setup User
        testStudent = new User("tester", "tester@quizspark.com");
        testStudent = userRepository.save(testStudent);

        // Security Setup
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testStudent, null, testStudent.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. Setup Question Bank & Question
        QuestionBank bank = new QuestionBank();
        bank.setTitle("Science Bank");
        bank.setUser(testStudent);
        bank = questionBankRepository.save(bank);

        Question q1 = new Question();
        q1.setDescription("Is water wet?");
        q1.setChoices(List.of("Yes", "No"));
        q1.setAnswer(List.of("0"));
        q1.setType("SINGLE_ANSWER");
        q1.setQuestionBank(bank);
        q1 = questionRepository.save(q1);

        // 3. Setup Practice Session
        testPractice = new Practice();
        testPractice.setUser(testStudent);
        testPractice.setBankNames(List.of(bank.getTitle()));
        
        PracticeQuestion pq1 = new PracticeQuestion();
        pq1.setQuestionId(q1.getId());
        pq1.setDescription(q1.getDescription());
        pq1.setChoices(q1.getChoices());
        pq1.setType(q1.getType());
        pq1.setCorrectAnswer(q1.getAnswer());
        
        List<PracticeQuestion> pqs = new ArrayList<>();
        pqs.add(pq1);
        testPractice.setQuestions(pqs);
        testPractice = practiceRepository.save(testPractice);
    }

    @Test
    void testSubmitAnswerUpdatesPractice() throws Exception {
        String practiceId = testPractice.getId().toString();
        
        // Submit answer for index 0
        String answerJson = "{\"index\": 0, \"answer\": [\"0\"], \"secondsToAnswer\": 10}";

        mockMvc.perform(post("/api/v1/practice/id/{practiceId}/answer", practiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctAnswer").isArray())
                .andExpect(jsonPath("$.correctAnswer[0]").value("0"));

        // Verify database state
        Practice updatedPractice = practiceRepository.findById(testPractice.getId()).orElseThrow();
        PracticeQuestion pq = updatedPractice.getQuestions().get(0);
        
        assertEquals(List.of("0"), pq.getUserAnswer());
        assertEquals(10, pq.getSecondsToAnswer());
    }
}
