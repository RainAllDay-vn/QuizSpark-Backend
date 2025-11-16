package com.quizspark.quizsparkserver.dto.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private final Question question;
    private UUID id;
    private List<String> tags;
    private String description;
    private int answer;
    private List<String> choices;
    private UUID questionBankId; // Include the ID of the question bank

    public QuestionDTO(Question question) {
        this.question = question;
        this.id = question.getId();
        this.tags = question.getTags();
        this.description = question.getDescription();
        this.answer = question.getAnswer();
        this.choices = question.getChoices();
        this.questionBankId = question.getQuestionBank() != null ? question.getQuestionBank().getId() : null;
    }
}
