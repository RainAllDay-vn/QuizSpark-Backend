package com.quizspark.quizsparkserver.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import com.quizspark.quizsparkserver.models.ChatItem;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatItemDTO {
    
    @NotNull(message = "Type is required")
    private ChatItemType type;
    
    private String description;
    
    private String content;
    
    private UUID fileId;
    
    private String fileName;
    
    private String fileType;

    public enum ChatItemType {
        MESSAGE, ATTACHMENT, CONTEXT
    }

    public ChatItem toChatItem(Integer orderIndex) {
        ChatItem item = new ChatItem();
        item.setType(ChatItem.ChatItemType.valueOf(this.getType().name()));
        item.setDescription(this.getDescription());
        item.setOrderIndex(orderIndex);
        item.setFileName(this.getFileName());
        item.setFileType(this.getFileType());
        // Content and File handling should be done by the service as they involve logic
        if (this.getContent() != null && this.getType() != ChatItemType.ATTACHMENT) {
             item.setContent(this.getContent());
        }
        return item;
    }
}