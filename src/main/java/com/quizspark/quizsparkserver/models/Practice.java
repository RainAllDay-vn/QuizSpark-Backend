package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(indexes = {
    @Index(name = "idx_practice_user_id", columnList = "user_id"),
    @Index(name = "idx_practice_date", columnList = "date")
})
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

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private QuestionBank questionBank;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "practice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PracticeQuestion> practiceQuestion;

    @Column(nullable = false)
    private boolean closed = false;

    @Column(nullable = false)
    private boolean revealAnswer = false;

    @Column(nullable = false)
    private Date date = new Date();

    @Column(nullable = false)
    private boolean endlessMode = false;

    @Column(columnDefinition = "TEXT")
    private String aiScratchPad;

    public Practice(User user, QuestionBank questionBank, List<Question> questions, boolean shuffleAnswer, boolean revealAnswer) {
        this(user, questionBank, questions, shuffleAnswer, revealAnswer, false);
    }

    public Practice(User user, QuestionBank questionBank, List<Question> questions, boolean shuffleAnswer, boolean revealAnswer, boolean endlessMode) {
        this.user = user;
        this.questionBank = questionBank;
        this.practiceQuestion = new ArrayList<>(questions.size());
        for (int i=0; i<questions.size(); i++) {
            Question question = questions.get(i);
            if (!question.getQuestionBank().getId().equals(questionBank.getId())) {
                throw new IllegalArgumentException("Question at index " + i + " does not belong to the specified QuestionBank");
            }
            this.practiceQuestion.add(new PracticeQuestion(this, i, question, shuffleAnswer));
        }
        this.revealAnswer = revealAnswer;
        this.endlessMode = endlessMode;
    }
}
