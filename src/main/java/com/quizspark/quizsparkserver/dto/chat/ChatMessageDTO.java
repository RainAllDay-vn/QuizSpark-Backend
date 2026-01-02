package com.quizspark.quizsparkserver.dto.chat;

import com.quizspark.quizsparkserver.models.ChatMessage.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private UUID id;
    private MessageRole role;
    private String content;
    private String model;
    private Integer messageIndex;
    private UUID fileId;
    private LocalDateTime createdAt;
}
