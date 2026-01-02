package com.quizspark.quizsparkserver.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    private String message;
    private UUID sessionId;
    private String model;
    private List<ChatFileDTO> files;
    private Integer index;
    private Boolean tts;
}
