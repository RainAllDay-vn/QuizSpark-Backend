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

import static com.quizspark.quizsparkserver.models.Question.QuestionType.MULTIPLE_ANSWER;
import static com.quizspark.quizsparkserver.models.Question.QuestionType.SINGLE_ANSWER;

@Entity
@Table(name = "practice_question", indexes = {
    @Index(name = "idx_practice_question_practice_id", columnList = "practice_id"),
    @Index(name = "idx_practice_question_question_id", columnList = "question_id")
})
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
                question.getQuestionType() == SINGLE_ANSWER
                        || question.getQuestionType() == MULTIPLE_ANSWER
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
        this.isCorrect = userAnswer.equals(getCorrectAnswer());
        this.secondsToAnswer = secondsToAnswer;
        if (this.practice.isRevealAnswer()) return getCorrectAnswer();
        else return null;
    }

    public List<String> getCorrectAnswer() {
        List<String> correctAnswer = new ArrayList<>(question.getAnswer().size());
        if (shuffleMapping!=null && shuffleMapping.size()>1){
            List<String> originalChoices = question.getChoices();
            switch (question.getQuestionType()){
                case SINGLE_ANSWER:
                case MULTIPLE_ANSWER:
                    List<Integer> originalAnswer = question.getAnswer()
                            .stream()
                            .map(Integer::parseInt)
                            .toList();
                    for (int i = 0; i < shuffleMapping.size(); i++) {
                        if (originalAnswer.contains(shuffleMapping.get(i))) {
                            correctAnswer.add(String.valueOf(i));
                        }
                    }
                    return correctAnswer;
                case FILL_THE_BLANK:
                    for (int i = 0; i < originalChoices.size(); i++) {
                        correctAnswer.add(originalChoices.get(shuffleMapping.get(i)));
                    }
                    return correctAnswer;
                default:
                    return question.getAnswer();
            }
        } else {
            return question.getAnswer();
        }
    }
}
