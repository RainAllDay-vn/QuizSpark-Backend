package com.quizspark.quizsparkserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizspark.quizsparkserver.config.AiConfig;
import com.quizspark.quizsparkserver.dto.questions.QuestionCreationDTO;
import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiService {
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Getter
    @Setter
    public static class AdaptiveGenerationResponse {
        private String scratchPad;
        private List<QuestionCreationDTO> questions;
    }

    public AiService(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public AdaptiveGenerationResponse generateAdaptiveQuestions(Practice practice, int count) {
        String scratchPad = practice.getAiScratchPad();
        List<PracticeQuestion> recentQuestions = practice.getPracticeQuestion().stream()
                .filter(PracticeQuestion::isClosed)
                .sorted((a, b) -> b.getIndex() - a.getIndex()) // Recent first
                .limit(10)
                .collect(Collectors.toList());

        try {
            String historyJson = objectMapper.writeValueAsString(recentQuestions.stream().map(pq -> {
                // Simplified history for AI
                return new Object() {
                    public String description = pq.getDescription();
                    public List<String> choices = pq.getChoices();
                    public List<String> answer = pq.getAnswer();
                    public List<String> userAnswer = pq.getUserAnswer();
                    public Boolean isCorrect = pq.getIsCorrect();
                };
            }).collect(Collectors.toList()));

            String userPrompt = String.format("""
                    Current Scratchpad: %s
                    
                    Recent Performance History:
                    %s
                    """, scratchPad, historyJson);

            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(String.format(AiConfig.ADAPTIVE_GENERATION_PROMPT, count)), new UserMessage(userPrompt)),
                    OpenAiChatOptions.builder().model(AiConfig.TEXT_MODEL).build());

            String responseContent = chatModel.call(prompt).getResult().getOutput().getContent();
            
            // Clean markdown blocks if present
            if (responseContent.startsWith("```json")) {
                responseContent = responseContent.substring(7);
            }
            if (responseContent.endsWith("```")) {
                responseContent = responseContent.substring(0, responseContent.length() - 3);
            }
            responseContent = responseContent.trim();

            return objectMapper.readValue(responseContent, AdaptiveGenerationResponse.class);
        } catch (Exception e) {
            log.error("Error generating adaptive questions", e);
            throw new RuntimeException("Failed to generate adaptive questions", e);
        }
    }
}
