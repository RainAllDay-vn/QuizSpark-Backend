package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.User;
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
 * TC-10: Manage Classes
 * 
 * Note: This test defines the behavior for a FUTURE feature.
 * It expects controllers for Classroom management to exist.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ManageClassesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void testTeacherCanCreateClassAndGetInviteCode() throws Exception {
        // 1. Create Class
        Map<String, String> classRequest = new HashMap<>();
        classRequest.put("name", "Computer Science 101");
        classRequest.put("description", "Intro to CS");

        String response = mockMvc.perform(post("/api/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 2. Extract Class ID (Assumes JSON response with id)
        String classId = objectMapper.readTree(response).get("id").asText();

        // 3. Verify Invite Code Endpoint
        mockMvc.perform(get("/api/v1/classes/" + classId + "/invite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").exists())
                .andExpect(jsonPath("$.inviteCode").isString());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void testStudentCannotCreateClass() throws Exception {
        Map<String, String> classRequest = new HashMap<>();
        classRequest.put("name", "Illegal Class");

        mockMvc.perform(post("/api/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest)))
                .andExpect(status().isForbidden());
    }
}
