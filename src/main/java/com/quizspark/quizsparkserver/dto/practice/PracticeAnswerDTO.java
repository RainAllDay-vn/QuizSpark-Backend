package com.quizspark.quizsparkserver.dto.practice;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PracticeAnswerDTO {
    @NotNull
    private int index;
    @NotNull
    private List<String> answer;
    @NotNull
    private int secondsToAnswer;
}
