package com.quizspark.quizsparkserver.models;

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
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @JsonManagedReference
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatSession(User user, String title) {
        this.user = user;
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }
}
