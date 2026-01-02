package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

import static com.quizspark.quizsparkserver.dto.questions.ChoicesAndAnswerProcessor.*;

@Setter
@Getter
public class QuestionUpdateDTO {
    private String questionType;

    private List<String> tagIds;

    private String description;

    private List<String> answer;

    private List<String> choices;

    public void updateQuestion(Question question) throws IllegalArgumentException {
        if (questionType != null) {
            question.setQuestionType(Question.QuestionType.valueOf(questionType));
        }
        if (choices != null || answer != null) {
            switch (question.getQuestionType()) {
                case SINGLE_ANSWER -> processSingleAnswerQuestion(question, choices, answer);
                case MULTIPLE_ANSWER -> processMultipleAnswerQuestion(question, choices, answer);
                case FILL_THE_BLANK -> processFillTheBlankQuestion(question, answer);
                case OPEN_ANSWER -> processOpenAnswerQuestion(question);
            }
        }
        if (tagIds != null) {
            question.getTags().clear();
            for (String tagId : tagIds) {
                Tag tag = question.getQuestionBank().getTagById(UUID.fromString(tagId))
                        .orElseThrow(
                                () -> new IllegalArgumentException("Tag with ID " + tagId + " not found in the bank"));
                question.getTags().add(tag);
            }
        }
        if (description != null)
            question.setDescription(description);
    }
}
