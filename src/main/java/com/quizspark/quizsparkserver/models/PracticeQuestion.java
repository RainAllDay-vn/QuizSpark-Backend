package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<Integer> shuffleMapping;

    @Column
    private Integer answer;

    private Boolean isCorrect;

    public PracticeQuestion(Question question, boolean shuffleAnswer) {
        this.question = question;
        this.answer = null;
        this.isCorrect = null;
        if (shuffleAnswer) {
            int choiceCount = question.getChoices().size();
            this.shuffleMapping = new ArrayList<>();
            for (int i = 0; i < choiceCount; i++) this.shuffleMapping.add(i);
            Collections.shuffle(this.shuffleMapping);
        } else {
            this.shuffleMapping = null;
        }
    }
}
