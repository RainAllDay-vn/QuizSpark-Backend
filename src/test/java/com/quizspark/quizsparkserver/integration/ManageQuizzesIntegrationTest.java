package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.dto.banks.QuestionBankCreationDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankUpdateDTO;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-08: Create / Manage Quizzes (Question Banks)
 * 
 * Verifies CRUD operations and access control for Question Banks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ManageQuizzesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionBankRepository bankRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "owner@example.com", roles = "STUDENT")
    void testCreateUpdateDeleteFlow() throws Exception {
        // 1. Create
        QuestionBankCreationDTO creationDTO = new QuestionBankCreationDTO();
        creationDTO.setName("New Quiz 1");
        creationDTO.setAccess("PRIVATE");
        creationDTO.setDescription("Test Description");

        String response = mockMvc.perform(post("/api/v1/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String bankId = objectMapper.readTree(response).get("id").asText();
        assertNotNull(bankId);

        // 2. Update
        QuestionBankUpdateDTO updateDTO = new QuestionBankUpdateDTO();
        updateDTO.setName("Updated Quiz 1");
        updateDTO.setStatus("PUBLISHED");

        mockMvc.perform(put("/api/v1/banks/single/" + bankId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Quiz 1"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        // 3. Delete
        mockMvc.perform(delete("/api/v1/banks/single/" + bankId))
                .andExpect(status().isOk());

        assertFalse(bankRepository.findById(UUID.fromString(bankId)).isPresent());
    }

    @Test
    @WithMockUser(username = "attacker@example.com", roles = "STUDENT")
    void testPermissionDenial() throws Exception {
        // Create a bank owned by a different user
        User owner = new User("owner", "owner@example.com");
        userRepository.save(owner);
        
        QuestionBank bank = new QuestionBank();
        bank.setName("Owner's Bank");
        bank.setAccess(QuestionBank.Access.PRIVATE);
        bank.setUser(owner);
        bank = bankRepository.save(bank);
        String bankId = bank.getId().toString();

        // Attempt to update as 'attacker'
        QuestionBankUpdateDTO updateDTO = new QuestionBankUpdateDTO();
        updateDTO.setName("Hacked Name");

        mockMvc.perform(put("/api/v1/banks/single/" + bankId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
        
        // Attempt to delete as 'attacker'
        mockMvc.perform(delete("/api/v1/banks/single/" + bankId))
                .andExpect(status().isForbidden());
    }
}
