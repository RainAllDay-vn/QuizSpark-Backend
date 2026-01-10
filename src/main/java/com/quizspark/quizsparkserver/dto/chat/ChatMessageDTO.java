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
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private UUID id;
    private MessageRole role;
    private List<ChatItemDTO> content;
    private String model;
    private Integer messageIndex;
    private LocalDateTime createdAt;
    
    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.role = message.getRole();
        this.model = message.getModel();
        this.messageIndex = message.getMessageIndex();
        this.createdAt = message.getCreatedAt();
        
        this.content = message.getItems().stream()
                .map(item -> ChatItemDTO.builder()
                        .type(ChatItemDTO.ChatItemType.valueOf(item.getType().name()))
                        .description(item.getDescription())
                        .content(item.getContent()) // Or handle retrieving base64 if needed, but usually we just send back text/ids
                        .fileId(item.getFile() != null ? item.getFile().getId() : null)
                        .fileName(item.getFileName())
                        .fileType(item.getFileType())
                        .build())
                .collect(Collectors.toList());
    }
}
