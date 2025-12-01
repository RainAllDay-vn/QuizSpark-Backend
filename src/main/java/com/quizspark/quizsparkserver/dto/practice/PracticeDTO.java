package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonIgnore
    private Practice practice;
    private UUID id;
    private List<PracticeQuestionDTO> questions;
    private boolean closed;
    private Date date;

    public PracticeDTO(Practice practice) {
        this.practice = practice;
        this.id = practice.getId();
        this.closed = practice.isClosed();
        this.date = practice.getDate();
    }

    public PracticeDTO appendQuestions() {
        this.questions = practice.getPracticeQuestion()
                .stream()
                .map(PracticeQuestionDTO::new)
                .toList();
        for (PracticeQuestionDTO questionDTO: questions) {
            if(questionDTO.getUserAnswer()!=null && practice.isRevealAnswer()) {
                questionDTO.appendCorrectAnswer();
            }
        }
        return this;
    }
}
