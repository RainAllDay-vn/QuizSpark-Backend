package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity@Table(name = "question_bank")
@Getter
@Setter
@NoArgsConstructor
public class QuestionBank {
    public enum Status {DRAFT, PUBLISHED}
    public enum Access {PUBLIC, PRIVATE}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Access access;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Date createdAt = new Date();

    private Float rating;

    @Column(nullable = false)
    private int numberOfAttempts = 0;

    // Relationship: One QuestionBank can contain many Questions
    @OneToMany(mappedBy = "questionBank", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
}
