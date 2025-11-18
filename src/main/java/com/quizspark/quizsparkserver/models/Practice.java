package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Practice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id")
    private QuestionBank questionBank;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "practice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PracticeQuestion> practiceQuestion;

    @Column(nullable = false)
    private boolean closed = false;

    @Column(nullable = false)
    private Date date = new Date();

    public Practice(User user, QuestionBank questionBank, List<Question> questions, boolean shuffleAnswer) {
        this.user = user;
        this.questionBank = questionBank;
        this.practiceQuestion = questions
                .stream()
                .map(question -> new PracticeQuestion(question, shuffleAnswer))
                .toList();
    }
}
