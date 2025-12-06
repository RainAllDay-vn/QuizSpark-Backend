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
@Table(indexes = {
    @Index(name = "idx_question_bank_id", columnList = "bank_id")
})
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

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "question_tag",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                @Index(name = "idx_question_tag_question_id", columnList = "question_id"),
                @Index(name = "idx_question_tag_tag_id", columnList = "tag_id")
            }
    )
    private List<Tag> tags = new ArrayList<>();

    @Column(nullable = false)
    @Lob
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
