package com.quizspark.quizsparkserver.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    private String message;
    private UUID sessionId;
    private String model;
    private UUID fileId;
    private Integer index;
    private Boolean tts;
}
