package com.quizspark.quizsparkserver.dto.chat;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFileDTO {
    private UUID id;
    private String fileName;
    private String fileType;
    private byte[] data;
}
