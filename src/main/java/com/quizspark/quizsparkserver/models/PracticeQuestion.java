package com.quizspark.quizsparkserver.models;

import com.quizspark.quizsparkserver.dto.tags.TagDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.quizspark.quizsparkserver.models.Question.QuestionType.MULTIPLE_ANSWER;
import static com.quizspark.quizsparkserver.models.Question.QuestionType.SINGLE_ANSWER;

@Entity
@Table(name = "practice_question", indexes = {
        @Index(name = "idx_practice_question_practice_id", columnList = "practice_id"),
        @Index(name = "idx_practice_question_question_id", columnList = "base_question_id")
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
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
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
    @JoinColumn(name = "base_question_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Question baseQuestion;

    @Column(name = "question_type")
    @Enumerated(EnumType.STRING)
    private Question.QuestionType questionType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> choices;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> answer;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<TagDTO> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> userAnswer = null;

    private Boolean isCorrect = null;

    private Integer secondsToAnswer = null;

    public PracticeQuestion(Practice practice, int index, Question baseQuestion, boolean shuffleAnswer) {
        this.id = new PracticeQuestionId(practice.getId(), index);
        this.practice = practice;
        this.baseQuestion = baseQuestion;

        // Copy question data
        this.questionType = baseQuestion.getQuestionType();
        this.description = baseQuestion.getDescription();
        this.tags = baseQuestion.getTags().stream().map(TagDTO::new).toList();

        // Apply shuffle if needed
        if (shuffleAnswer && (questionType == SINGLE_ANSWER || questionType == MULTIPLE_ANSWER)) {
            applyShuffleAndCopy(baseQuestion);
        } else {
            this.choices = baseQuestion.getChoices() != null ? new ArrayList<>(baseQuestion.getChoices()) : null;
            this.answer = baseQuestion.getAnswer() != null ? new ArrayList<>(baseQuestion.getAnswer()) : null;
        }
    }

    private void applyShuffleAndCopy(Question question) {
        List<String> originalChoices = question.getChoices();
        List<String> originalAnswer = question.getAnswer();
        int choiceCount = originalChoices.size();

        // Create shuffle mapping
        List<Integer> shuffleMapping = new ArrayList<>();
        for (int i = 0; i < choiceCount; i++) shuffleMapping.add(i);
        Collections.shuffle(shuffleMapping);

        // Apply to choices
        this.choices = new ArrayList<>(choiceCount);
        for (int originalIdx : shuffleMapping) {
            this.choices.add(originalChoices.get(originalIdx));
        }

        // Map answer to new indices
        Set<Integer> originalAnswerIndices = originalAnswer.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        this.answer = new ArrayList<>();
        for (int newIdx = 0; newIdx < shuffleMapping.size(); newIdx++) {
            if (originalAnswerIndices.contains(shuffleMapping.get(newIdx))) {
                this.answer.add(String.valueOf(newIdx));
            }
        }
    }

    public boolean isClosed() {
        if (practice.isClosed())
            return true;
        return practice.isRevealAnswer() && userAnswer != null && !userAnswer.isEmpty();
    }

    public void updateUserAnswer(List<String> userAnswer, int secondsToAnswer) {
        if (isClosed())
            return;

        this.userAnswer = userAnswer;
        this.secondsToAnswer = secondsToAnswer;

        if (userAnswer == null) {
            this.isCorrect = false;
            return;
        }

        if (questionType == SINGLE_ANSWER || questionType == MULTIPLE_ANSWER) {
            this.isCorrect = new HashSet<>(userAnswer).equals(new HashSet<>(this.answer));
        } else {
            this.isCorrect = userAnswer.equals(this.answer);
        }
    }
}
