package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.*;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Bypass JWT filter for testing
@ActiveProfiles("test")
@Transactional
public class PracticeResultsE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private PracticeQuestionRepository practiceQuestionRepository;

    private User testUser;
    private QuestionBank testBank;

    @BeforeEach
    void setUp() {
        // 1. Create Test Student
        testUser = new User("teststudent", "student@test.com");
        testUser.setRole("ROLE_STUDENT");
        testUser = userRepository.save(testUser);

        // Mock Security Context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. Create Question Bank
        testBank = new QuestionBank();
        testBank.setName("Test Science Bank");
        testBank.setUser(testUser);
        testBank = questionBankRepository.save(testBank);

        // 3. Create Questions
        Question q1 = createQuestion("What is 2+2?", List.of("4"), List.of("3", "4", "5"));
        Question q2 = createQuestion("What is the capital of France?", List.of("Paris"), List.of("Paris", "London", "Berlin"));
        Question q3 = createQuestion("Is the earth round?", List.of("Yes"), List.of("Yes", "No"));

        // 4. Seed a Practice Session (Completed)
        Practice practice = new Practice(testUser, List.of(q1, q2, q3), false, true);
        practice.setClosed(true);
        practice = practiceRepository.save(practice);

        // 5. Set Answers (2 Correct, 1 Incorrect)
        List<PracticeQuestion> pqs = practiceQuestionRepository.findByPracticeId(practice.getId());
        
        // Q1: Correct
        pqs.get(0).updateUserAnswer(List.of("4"), 10);
        // Q2: Correct
        pqs.get(1).updateUserAnswer(List.of("Paris"), 15);
        // Q3: Incorrect
        pqs.get(2).updateUserAnswer(List.of("No"), 5);

        practiceQuestionRepository.saveAll(pqs);
    }

    private Question createQuestion(String desc, List<String> ans, List<String> choices) {
        Question q = new Question();
        q.setDescription(desc);
        q.setAnswer(ans);
        q.setChoices(choices);
        q.setQuestionType(Question.QuestionType.SINGLE_ANSWER);
        q.setQuestionBank(testBank);
        return questionRepository.save(q);
    }

    @Test
    void testViewResultsAndProgress() throws Exception {
        // --- Requirement 1: Overall Statistics (Dashboard) ---
        mockMvc.perform(get("/api/v1/users/me/statistic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestionsAnswered").value(3))
                .andExpect(jsonPath("$.totalQuestionBanks").value(1))
                .andExpect(jsonPath("$.totalTimeSpentInSeconds").value(30)); // 10 + 15 + 5

        // --- Requirement 2: Past Practice List ---
        mockMvc.perform(get("/api/v1/practice/me")
                        .param("isClosed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].bankNames[0]").value("Test Science Bank"))
                .andExpect(jsonPath("$.content[0].closed").value(true));

        // --- Requirement 3: Practice Results Detail ---
        UUID practiceId = practiceRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/v1/practice/id/" + practiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfQuestion").value(3))
                .andExpect(jsonPath("$.timeInSeconds").value(30));
        
        // Note: SummarySection.tsx calculates accuracy on the fly: (2/3) * 100 = 67%
    }
}
