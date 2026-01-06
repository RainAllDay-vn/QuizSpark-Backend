package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-07: AI Quiz Recommendations
 * 
 * Note: This test defines the behavior for a FUTURE feature.
 * It expects a GET /api/v1/recommendations/me endpoint to exist.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionBankRepository bankRepository;

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void testGetRecommendationsBasedOnWeakAreas() throws Exception {
        // 1. Setup User and History
        // (In a real implementation, we would seed PracticeHistory with low scores in a specific tag)
        
        // 2. Setup Question Banks
        User admin = new User("admin", "admin@example.com");
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        QuestionBank weakAreaBank = new QuestionBank();
        weakAreaBank.setName("Advanced Chemistry");
        weakAreaBank.setAccess(QuestionBank.Access.PUBLIC);
        weakAreaBank.setStatus(QuestionBank.Status.PUBLISHED);
        weakAreaBank.setTags(java.util.List.of(new Tag("Chemistry")));
        bankRepository.save(weakAreaBank);

        // 3. Execution & Assertion
        // We expect the system to recommend the "Chemistry" bank if history shows low chemistry proficiency
        mockMvc.perform(get("/api/v1/recommendations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].name").value("Advanced Chemistry"));
    }
}
