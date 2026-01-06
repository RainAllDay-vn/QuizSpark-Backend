package com.quizspark.quizsparkserver.integration;

import com.google.firebase.auth.FirebaseAuthException;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import com.quizspark.quizsparkserver.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-05: Authentication Flow
 * 
 * Verifies that the JwtAuthenticationFilter correctly handles Bearer tokens
 * and identifies user roles based on their existence in the local database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetMeWithValidTokenForExistingUser() throws Exception {
        // Setup existing user
        User user = new User("existing_user", "test@example.com");
        user.setRole("ROLE_STUDENT");
        userRepository.save(user);

        // Mock JwtUtil to return this user's email
        when(jwtUtil.validateToken("valid-token")).thenReturn("test@example.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("existing_user"))
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"));
    }

    @Test
    void testGetMeWithValidTokenForNewUser() throws Exception {
        // Mock JwtUtil to return an email that doesn't exist in our DB
        when(jwtUtil.validateToken("new-user-token")).thenReturn("new@example.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer new-user-token"))
                .andExpect(status().isNotFound()); // UserController returns 404 for ROLE_NEW
    }

    @Test
    void testAccessDeniedWithInvalidToken() throws Exception {
        // Mock JwtUtil to throw exception (invalid token)
        when(jwtUtil.validateToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        // If the filter throws a RuntimeException in JwtAuthenticationFilter line 49, 
        // Spring's error handler will likely return 500 or 401 depending on config.
        // However, if the token is simply missing or malformed, it proceeds without authentication.
        
        mockMvc.perform(get("/api/v1/practice/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isInternalServerError()); // Per current code's 'throw new RuntimeException(e)'
    }

    @Test
    void testUnauthorizedAccessNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/practice/me"))
                .andExpect(status().isForbidden()); // SecurityConfig requires role for this endpoint
    }
}
