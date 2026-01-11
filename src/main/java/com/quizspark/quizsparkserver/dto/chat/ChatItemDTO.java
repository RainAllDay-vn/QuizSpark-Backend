package com.quizspark.quizsparkserver.dto.chat;

import com.quizspark.quizsparkserver.models.ChatItem;
import com.quizspark.quizsparkserver.models.DbFile;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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

    public ChatItem toChatItem(Integer orderIndex, String content) {
        ChatItem item = createBaseChatItem(orderIndex);
        item.setContent(content);
        item.setFile(null);
        return item;
    }

    public ChatItem toChatItem(Integer orderIndex, DbFile file) {
        ChatItem item = createBaseChatItem(orderIndex);
        item.setContent(null);
        item.setFile(file);
        return item;
    }

    private ChatItem createBaseChatItem(Integer orderIndex) {
        ChatItem item = new ChatItem();
        item.setType(ChatItem.ChatItemType.valueOf(this.getType().name()));
        item.setDescription(this.getDescription());
        item.setOrderIndex(orderIndex);
        return item;
    }
}