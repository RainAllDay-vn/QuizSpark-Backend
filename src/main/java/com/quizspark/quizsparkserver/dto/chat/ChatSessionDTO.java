package com.quizspark.quizsparkserver.dto.chat;

import com.quizspark.quizsparkserver.models.ChatSession;
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
public class ChatSessionDTO {
    private UUID id;
    private String title;
    private LocalDateTime createdAt;

    public ChatSessionDTO(ChatSession session) {
        this.id = session.getId();
        this.title = session.getTitle();
        this.createdAt = session.getCreatedAt();
    }
}
