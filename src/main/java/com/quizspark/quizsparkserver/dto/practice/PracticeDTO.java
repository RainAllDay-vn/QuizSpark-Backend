package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.PracticeQuestion;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeDTO {
    @JsonIgnore
    private Practice practice;
    private UUID id;
    private List<String> bankNames;
    private List<TagDTO> tags;
    private int timeInSeconds;
    private int numberOfQuestion;
    private List<PracticeQuestionDTO> questions;
    private boolean closed;
    private Date date;
    private boolean endlessMode;
    private String aiScratchPad;

    public PracticeDTO(Practice practice) {
        this.practice = practice;
        this.id = practice.getId();
        if (practice.getQuestionBank() != null) {
            this.bankNames = List.of(practice.getQuestionBank().getName());
        } else {
            this.bankNames = Collections.emptyList();
        }

        if (practice.getPracticeQuestion() != null && !practice.getPracticeQuestion().isEmpty()) {
            this.tags = practice.getPracticeQuestion()
                    .stream()
                    .map(PracticeQuestion::getTags)
                    .flatMap(List::stream)
                    .distinct()
                    .sorted()
                    .toList();
            this.timeInSeconds = practice.getPracticeQuestion()
                    .stream()
                    .map(PracticeQuestion::getSecondsToAnswer)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
        } else {
            this.tags = Collections.emptyList();
            this.timeInSeconds = 0;
        }
        this.numberOfQuestion = practice.getPracticeQuestion().size();
        this.closed = practice.isClosed();
        this.date = practice.getDate();
        this.endlessMode = practice.isEndlessMode();
        this.aiScratchPad = practice.getAiScratchPad();
    }

    public PracticeDTO appendQuestions() {
        this.questions = practice.getPracticeQuestion()
                .stream()
                .map(PracticeQuestionDTO::new)
                .toList();
        for (PracticeQuestionDTO questionDTO : questions) {
            if (questionDTO.getUserAnswer() != null && practice.isRevealAnswer()) {
                questionDTO.appendCorrectAnswer();
                questionDTO.appendComments();
            }
        }
        return this;
    }
}
