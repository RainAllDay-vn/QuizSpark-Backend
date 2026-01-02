package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.quizspark.quizsparkserver.dto.questions.ChoicesAndAnswerProcessor.*;

@Getter
@Setter
public class QuestionCreationDTO {
    private Question question;
    private QuestionBank questionBank;

    @NotBlank
    private String questionType;

    private List<String> tags;

    @NotBlank(message = "Question description must not be blank")
    private String description;

    private List<String> answer;

    private List<String> choices;

    public Question toQuestion(QuestionBank questionBank) throws IllegalArgumentException {
        question = new Question();
        question.setQuestionBank(questionBank);
        question.setQuestionType(Question.QuestionType.valueOf(questionType));
        switch (question.getQuestionType()) {
            case SINGLE_ANSWER -> processSingleAnswerQuestion(question, choices, answer);
            case MULTIPLE_ANSWER -> processMultipleAnswerQuestion(question, choices, answer);
            case FILL_THE_BLANK -> processFillTheBlankQuestion(question, answer);
            case OPEN_ANSWER -> processOpenAnswerQuestion(question);
        }
        question.setTags(new ArrayList<>());
        if (tags != null) {
            for (String tagId : tags) {
                Tag tag = questionBank.getTagById(UUID.fromString(tagId))
                        .orElse(new Tag(tagId));
                question.getTags().add(tag);
            }
        }
        question.setDescription(description);
        return question;
    }
}
