package com.quizspark.quizsparkserver.dto.chat;

import com.quizspark.quizsparkserver.models.ChatMessage.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String chunk;
    private UUID sessionId;
    private MessageRole role;
    private String status; // thinking, data, finish, error
}
