package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity(name = "practice_question")
@Getter
@Setter
@NoArgsConstructor
public class PracticeQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    @Column(nullable = false)
    private int index;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @JdbcTypeCode(SqlTypes.JSON)
    private Integer[] shuffleMapping;

    private Integer answer;

    private Boolean isCorrect;
}
