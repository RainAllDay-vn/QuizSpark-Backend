package com.quizspark.quizsparkserver.dto.practice;

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
    private UUID id;
    private List<PracticeQuestionDTO> questions;
    private boolean closed;
    private Date date;

    public PracticeDTO(Practice practice) {
        this.id = practice.getId();
        this.questions = practice.getPracticeQuestion()
                .stream()
                .map(PracticeQuestionDTO::new)
                .toList();
        if (!practice.isRevealAnswer()) questions.forEach(question -> question.setAnswer(null));
        this.closed = practice.isClosed();
        this.date = practice.getDate();
    }
}
