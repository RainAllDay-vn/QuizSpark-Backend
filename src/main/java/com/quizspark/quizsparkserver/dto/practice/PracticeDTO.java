package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.models.Practice;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeDTO {
    private UUID id;
    private QuestionBankDTO questionBank;
    private List<PracticeQuestionDTO> questions;
    private boolean closed;
    private Date date;

    public PracticeDTO(Practice practice, boolean revealAnswer) {
        this.id = practice.getId();
        this.questionBank = new QuestionBankDTO(practice.getQuestionBank());
        this.questions = practice.getPracticeQuestion()
                .stream()
                .map(PracticeQuestionDTO::new)
                .toList();
        if (!revealAnswer) questions.forEach(question -> question.setAnswer(null));
        this.closed = practice.isClosed();
        this.date = practice.getDate();
    }
}
