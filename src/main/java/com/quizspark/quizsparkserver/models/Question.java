package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity(name = "question")
@Getter
@Setter
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private QuestionBank questionBank;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int answer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> choices;

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass()!=this.getClass()) return false;
        Question question = (Question) obj;
        return this.getId().equals(question.getId());
    }
}
