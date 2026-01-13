package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
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
@SQLDelete(sql = "UPDATE question SET deleted = true WHERE id = ?")
public class Question {
    public enum QuestionType {
        SINGLE_ANSWER,
        MULTIPLE_ANSWER,
        FILL_THE_BLANK,
        OPEN_ANSWER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuestionBank questionBank;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "question_tag", joinColumns = @JoinColumn(name = "question_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE")), inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE")), indexes = {
            @Index(name = "idx_question_tag_question_id", columnList = "question_id"),
            @Index(name = "idx_question_tag_tag_id", columnList = "tag_id")
    })
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Tag> tags = new ArrayList<>();

    @Column(nullable = false)
    @Lob
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> answer;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> choices;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdDate DESC")
    private List<QuestionComment> comments;

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;
        Question question = (Question) obj;
        return this.getId().equals(question.getId());
    }
}
