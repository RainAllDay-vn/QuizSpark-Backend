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

    public PracticeQuestion(Question question, boolean shuffleAnswer) {
        this.question = question;
        this.answer = null;
        this.isCorrect = null;
        if (shuffleAnswer) {
            int choiceCount = question.getChoices().size();
            this.shuffleMapping = new Integer[choiceCount];
            for (int i = 0; i < choiceCount; i++) this.shuffleMapping[i] = i;
            for (int i = choiceCount - 1; i > 0; i--) {
                int j = (int) (Math.random() * (i + 1));
                int temp = this.shuffleMapping[i];
                this.shuffleMapping[i] = this.shuffleMapping[j];
                this.shuffleMapping[j] = temp;
            }
        } else {
            this.shuffleMapping = null;
        }
    }
}
