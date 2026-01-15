package com.quizspark.quizsparkserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String TEXT_MODEL = "gemini-3-pro-high";

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

    private static final String ADAPTIVE_GENERATION_PROMPT = """
            # Role
            You are an Adaptive Learning Engine. Your goal is to analyze a user's performance and provide a continuous stream of tailored educational content.
            
            # Input
            1. **Scratchpad**: Your own summary of the user's progress from the previous turn. Use this to maintain context.
            2. **Recent History**: The last 5-10 questions the user attempted, including their description, choices, and whether they were correct.
            
            # Task
            1. **Update Scratchpad**: Analyze the recent history. Identify persistent weaknesses, improving trends, or areas of mastery. Write a concise summary (the "scratchpad") for your future self.
            2. **Generate Questions**: Generate %d new questions that:
               - Target identified weak spots (Reinforcement).
               - Introduce slightly more difficult variations of mastered concepts (Progression).
               - Maintain the core themes of the original Question Bank.
            
            # Output Format
            Return valid JSON only, following this structure:
            {
              "scratchPad": "Your updated summary here...",
              "questions": [
                {
                  "questionType": "SINGLE_ANSWER",
                  "tags": ["AI_GENERATED"],
                  "description": "Question text...",
                  "choices": ["A", "B", "C", "D"],
                  "answer": ["0"]
                }
              ]
            }
            """;

    public AdaptiveGenerationResponse generateAdaptiveQuestions(Practice practice, int count) {
        String scratchPad = practice.getAiScratchPad();
        List<PracticeQuestion> recentQuestions = practice.getPracticeQuestion().stream()
                .filter(PracticeQuestion::isClosed)
                .sorted((a, b) -> b.getId().getIndex() - a.getId().getIndex()) // Recent first
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
                    List.of(new SystemMessage(String.format(ADAPTIVE_GENERATION_PROMPT, count)), new UserMessage(userPrompt)),
                    OpenAiChatOptions.builder().model(TEXT_MODEL).build());

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
