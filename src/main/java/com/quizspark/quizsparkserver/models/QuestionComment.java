package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(indexes = {
    @Index(name = "idx_question_comment_question", columnList = "question_id"),
    @Index(name = "idx_question_comment_user", columnList = "user_id"),
    @Index(name = "idx_question_comment_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
public class QuestionComment{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "is_answer", nullable = false)
    private boolean isAnswer;

    @Column(name = "is_ai", nullable = false)
    private boolean isAI;
}
