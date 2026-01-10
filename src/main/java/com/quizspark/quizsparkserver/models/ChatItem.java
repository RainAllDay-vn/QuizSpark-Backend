package com.quizspark.quizsparkserver.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "chat_items")
@Getter
@Setter
@NoArgsConstructor
public class ChatItem {
    
    public enum ChatItemType {
        MESSAGE, ATTACHMENT, CONTEXT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    @JsonBackReference
    private ChatMessage message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatItemType type;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private DbFile file;
    
    @Column(nullable = false)
    private Integer orderIndex;
    
    @Column
    private String fileName;
    
    @Column
    private String fileType;

    public ChatItem(ChatMessage message, ChatItemType type, String content, DbFile file, Integer orderIndex) {
        this.message = message;
        this.type = type;
        this.content = content;
        this.file = file;
        this.orderIndex = orderIndex;
    }
}