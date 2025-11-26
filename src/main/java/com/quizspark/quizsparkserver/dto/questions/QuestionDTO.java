package com.quizspark.quizsparkserver.dto.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDTO {
    private UUID id;
    private UUID questionBankId;
    private String questionType;
    private List<String> tags;
    private String description;
    private List<String> answer;
    private List<String> choices;
    private String explanation;

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.questionBankId = question.getQuestionBank().getId();
        this.questionType = question.getQuestionType().toString();
        this.tags = question.getTags();
        this.description = question.getDescription();
        this.answer = question.getAnswer();
        this.choices = question.getChoices();
        this.explanation = question.getExplanation();
    }
}
