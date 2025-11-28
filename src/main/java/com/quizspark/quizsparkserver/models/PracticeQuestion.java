package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "practice_question")
@Getter
@Setter
@NoArgsConstructor
public class PracticeQuestion {

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PracticeQuestionId implements Serializable {
        private UUID practiceId;
        private Integer index;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PracticeQuestionId that = (PracticeQuestionId) o;
            return Objects.equals(practiceId, that.practiceId) &&
                    Objects.equals(index, that.index);
        }

        @Override
        public int hashCode() {
            return Objects.hash(practiceId, index);
        }
    }

    @EmbeddedId
    private PracticeQuestionId id = new PracticeQuestionId();

    @MapsId("practiceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Integer> shuffleMapping;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> userAnswer = null;

    private Boolean isCorrect = null;

    private Integer secondsToAnswer = null;

    public PracticeQuestion(Practice practice, int index, Question question, boolean shuffleAnswer) {
        this.id = new PracticeQuestionId(practice.getId(), index);
        this.practice = practice;
        this.question = question;
        if (shuffleAnswer && (
                question.getQuestionType() == Question.QuestionType.SINGLE_ANSWER
                        || question.getQuestionType() == Question.QuestionType.MULTIPLE_ANSWER
        )) {
            int choiceCount = question.getChoices().size();
            this.shuffleMapping = new ArrayList<>();
            for (int i = 0; i < choiceCount; i++) this.shuffleMapping.add(i);
            Collections.shuffle(this.shuffleMapping);
        } else {
            this.shuffleMapping = null;
        }
    }

    public boolean isClosed() {
        if (practice.isClosed()) return true;
        return practice.isRevealAnswer() && userAnswer != null && !userAnswer.isEmpty();
    }

    public List<String> answer(List<String> userAnswer, int secondsToAnswer) {
        if (isClosed()) return null;
        this.userAnswer = userAnswer;
        this.isCorrect = userAnswer.equals(question.getAnswer());
        this.secondsToAnswer = secondsToAnswer;
        if (this.practice.isRevealAnswer()) return question.getAnswer();
        else return null;
    }
}
