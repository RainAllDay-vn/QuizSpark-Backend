package com.quizspark.quizsparkserver.dto.chat;

import com.quizspark.quizsparkserver.models.ChatMessage;
import com.quizspark.quizsparkserver.models.ChatMessage.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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
    private List<UUID> fileIds;
    private LocalDateTime createdAt;

    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.role = message.getRole();
        this.content = message.getContent();
        this.model = message.getModel();
        this.messageIndex = message.getMessageIndex();
        this.fileIds = message.getFiles() != null
                ? message.getFiles().stream().map(com.quizspark.quizsparkserver.models.DbFile::getId)
                        .collect(java.util.stream.Collectors.toList())
                : new java.util.ArrayList<>();
        this.createdAt = message.getCreatedAt();
    }
}
