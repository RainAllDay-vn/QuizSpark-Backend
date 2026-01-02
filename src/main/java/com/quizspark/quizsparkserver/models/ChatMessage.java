package com.quizspark.quizsparkserver.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    public enum MessageRole {
        USER, ASSISTANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonBackReference
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(nullable = false)
    private Integer messageIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String model;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "chat_message_files", joinColumns = @JoinColumn(name = "message_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<DbFile> files = new java.util.ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ChatMessage(ChatSession session, MessageRole role, String content, String model, List<DbFile> files,
            Integer messageIndex) {
        this.session = session;
        this.role = role;
        this.content = content;
        this.model = model;
        this.files = files;
        this.messageIndex = messageIndex;
        this.createdAt = LocalDateTime.now();
    }
}
