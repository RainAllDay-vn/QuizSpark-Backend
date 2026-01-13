package com.quizspark.quizsparkserver.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @JsonManagedReference
    private List<ChatItem> items = new ArrayList<>();

    private String model;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage(ChatSession session, MessageRole role, String model, Integer messageIndex) {
        this.session = session;
        this.role = role;
        this.model = model;
        this.messageIndex = messageIndex;
    }
    
    public void addItem(ChatItem item) {
        items.add(item);
        item.setMessage(this);
    }

    // Helper method to get files from items for deletion logic
    public List<DbFile> getFiles() {
        List<DbFile> files = new ArrayList<>();
        if (items != null) {
            for (ChatItem item : items) {
                if (item.getFile() != null) {
                    files.add(item.getFile());
                }
            }
        }
        return files;
    }
}
