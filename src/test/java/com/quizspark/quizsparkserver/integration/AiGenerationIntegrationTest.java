package com.quizspark.quizsparkserver.integration;

import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.repositories.FileRepository;
import com.quizspark.quizsparkserver.services.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TC-06: AI Question Generation Flow
 * 
 * Verifies that the system can parse an uploaded text file (topic) 
 * and return a stream of generated questions within 10 seconds.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiGenerationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatModel chatModel;

    @MockBean
    private FileService fileService;

    @Test
    void testGenerateQuestionsFromTextFile() throws Exception {
        // Setup mock file (Topic: "Photosynthesis")
        String fileId = UUID.randomUUID().toString();
        DbFile mockFile = new DbFile();
        mockFile.setId(UUID.fromString(fileId));
        mockFile.setFileType("text/plain");
        mockFile.setData("Topic: Photosynthesis. Explain the light-dependent reactions.".getBytes());
        
        when(fileService.getFileEntity(fileId)).thenReturn(mockFile);

        // Mock LLM Response (Streaming JSON objects)
        String jsonQuestion = "{\"questionType\":\"SINGLE_ANSWER\",\"tags\":[\"AI_GENERATED\"],\"description\":\"Question 1\",\"answer\":[\"0\"],\"choices\":[\"A\",\"B\"]}";
        Flux<ChatResponse> responseFlux = Flux.just(
                new ChatResponse(java.util.List.of(new Generation(jsonQuestion)))
        );
        
        when(chatModel.stream(any())).thenReturn(responseFlux);

        long startTime = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/api/v1/ai/parse/file/" + fileId)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andReturn();

        long duration = System.currentTimeMillis() - startTime;
        
        // Performance check: Must be under 10 seconds (10000ms)
        assertTrue(duration < 10000, "AI Generation took too long: " + duration + "ms");

        String content = result.getResponse().getContentAsString();
        
        // Verify stream structure
        assertTrue(content.contains("data:{\"status\":\"start\"}"), "Stream should start");
        assertTrue(content.contains("data:{\"status\":\"thinking\"}"), "Stream should indicate thinking");
        assertTrue(content.contains("data:{\"status\":\"data\",\"data\":\"" + jsonQuestion.replace("\"", "\\\"") + "\"}"), "Stream should contain question data");
        assertTrue(content.contains("data:{\"status\":\"finish\"}"), "Stream should finish");
    }
}
