package com.quizspark.quizsparkserver.dto.agent;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class ChatMessageDTO {
    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
    private String text;

    @NotBlank(message = "Sender cannot be empty")
    @Pattern(regexp = "^(USER|BOT)$", message = "Sender must be 'USER' or 'USER'")
    private String sender;

    @NotNull(message = "Timestamp cannot be null")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    private Date timestamp;
}
