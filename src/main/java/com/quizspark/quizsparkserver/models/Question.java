package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {
    public enum QuestionType{
        SINGLE_ANSWER,
        MULTIPLE_ANSWER,
        FILL_THE_BLANK,
        OPEN_ANSWER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private QuestionBank questionBank;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> answer;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> choices;

    private String explanation;

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass()!=this.getClass()) return false;
        Question question = (Question) obj;
        return this.getId().equals(question.getId());
    }
}
