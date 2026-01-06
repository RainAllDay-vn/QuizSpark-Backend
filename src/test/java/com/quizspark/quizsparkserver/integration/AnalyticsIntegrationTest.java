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
 * TC-09: View Analytics
 * 
 * Note: This test defines the behavior for a FUTURE feature.
 * It expects a GET /api/v1/analytics/bank/{bankId} endpoint to exist.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionBankRepository bankRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "author@example.com", roles = "STUDENT")
    void testGetAggregateAnalyticsForBank() throws Exception {
        // 1. Setup Author and Bank
        User author = new User("author", "author@example.com");
        author.setRole("ROLE_STUDENT");
        userRepository.save(author);

        QuestionBank bank = new QuestionBank();
        bank.setName("Analytics Test Quiz");
        bank.setUser(author);
        bank.setAccess(QuestionBank.Access.PRIVATE);
        bank = bankRepository.save(bank);
        String bankId = bank.getId().toString();

        // 2. Setup Mock Data (Simulated by the future service)
        // In a real implementation, we would seed Practice entities from multiple users.

        // 3. Execution & Assertion
        mockMvc.perform(get("/api/v1/analytics/bank/" + bankId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.highestScore").exists())
                .andExpect(jsonPath("$.lowestScore").exists())
                .andExpect(jsonPath("$.totalCompletions").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.questionBreakdown").isArray());
    }

    @Test
    @WithMockUser(username = "stranger@example.com", roles = "STUDENT")
    void testAnalyticsAccessDeniedForNonAuthor() throws Exception {
        // Create bank owned by 'author'
        User author = new User("author", "author@example.com");
        userRepository.save(author);
        QuestionBank bank = new QuestionBank();
        bank.setName("Private Bank");
        bank.setUser(author);
        bank = bankRepository.save(bank);

        // Attempt to access as 'stranger'
        mockMvc.perform(get("/api/v1/analytics/bank/" + bank.getId()))
                .andExpect(status().isForbidden());
    }
}
