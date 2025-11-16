package com.quizspark.quizsparkserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionBank;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionBankDTO {
    @JsonIgnore
    private final QuestionBank questionBank;
    private UUID id;
    private String name;
    private String description;
    private String access;
    private String status;
    private Date createdAt;
    private Float rating;
    private int numberOfQuestions;
    private int numberOfAttempts;
    private String username; // Owner's username
    private List<QuestionDTO> questions; // List of questions in the question bank

    public QuestionBankDTO(QuestionBank questionBank) {
        this.questionBank = questionBank;
        this.id = questionBank.getId();
        this.name = questionBank.getName();
        this.description = questionBank.getDescription();
        this.access = questionBank.getAccess() != null ? questionBank.getAccess().toString() : null;
        this.status = questionBank.getStatus() != null ? questionBank.getStatus().toString() : null;
        this.createdAt = questionBank.getCreatedAt();
        this.rating = questionBank.getRating();
        this.numberOfQuestions = questionBank.getNumberOfQuestions();
        this.numberOfAttempts = questionBank.getNumberOfAttempts();
        this.username = questionBank.getUser() != null ? questionBank.getUser().getUsername() : null;
    }

    public QuestionBankDTO appendQuestions() {
        questions = new LinkedList<>();
        for (Question question: questionBank.getQuestions()) {
            questions.add(new QuestionDTO(question));
        }
        return this;
    }
}
