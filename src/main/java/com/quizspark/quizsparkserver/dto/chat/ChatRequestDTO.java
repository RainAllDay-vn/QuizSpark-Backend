package com.quizspark.quizsparkserver.dto.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    
    private UUID sessionId;
    
    private String model;
    
    private Integer index;
    
    private Boolean tts;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<ChatItemDTO> items;
}
