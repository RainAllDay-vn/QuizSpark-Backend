package com.quizspark.quizsparkserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatisticDTO {
    private int totalQuestionBanks;
    private int totalQuestionsAnswered;
    private int totalTimeSpentInSeconds;
}
