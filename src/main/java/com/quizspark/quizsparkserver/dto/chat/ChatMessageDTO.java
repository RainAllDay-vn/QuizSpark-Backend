package com.quizspark.quizsparkserver.dto.chat;

import com.quizspark.quizsparkserver.models.ChatMessage;
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

    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.role = message.getRole();
        this.content = message.getContent();
        this.model = message.getModel();
        this.messageIndex = message.getMessageIndex();
        this.fileId = message.getFile() != null ? message.getFile().getId() : null;
        this.createdAt = message.getCreatedAt();
    }
}
