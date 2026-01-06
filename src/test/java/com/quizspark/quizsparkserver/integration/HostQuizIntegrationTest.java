package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-11: Host Quiz Session
 * 
 * Note: This test defines the behavior for a FUTURE feature.
 * It expects controllers for Live Session management to exist.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class HostQuizIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionBankRepository bankRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void testTeacherCanHostSessionAndStart() throws Exception {
        // 1. Setup Author and Bank
        User author = new User("teacher", "teacher@example.com");
        author.setRole("ROLE_TEACHER");
        userRepository.save(author);

        QuestionBank bank = new QuestionBank();
        bank.setName("Live Quiz");
        bank.setUser(author);
        bank = bankRepository.save(bank);

        // 2. Go Live
        Map<String, String> sessionRequest = new HashMap<>();
        sessionRequest.put("bankId", bank.getId().toString());

        String response = mockMvc.perform(post("/api/v1/sessions/live")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 3. Extract Session ID and PIN
        String sessionId = objectMapper.readTree(response).get("id").asText();
        String pin = objectMapper.readTree(response).get("pin").asText();

        // 4. Verify Status (simulate student count)
        mockMvc.perform(get("/api/v1/sessions/" + sessionId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pin").value(pin))
                .andExpect(jsonPath("$.studentCount").exists());

        // 5. Start Session
        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
