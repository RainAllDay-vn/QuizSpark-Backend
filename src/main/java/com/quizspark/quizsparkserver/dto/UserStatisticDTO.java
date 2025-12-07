package com.quizspark.quizsparkserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.dto.repositories.PracticeStatisticsDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatisticDTO {
    private long totalQuestionBanks;
    private long totalQuestionsAnswered;
    private long totalTimeSpentInSeconds;

    public void updatePracticeStatistics(PracticeStatisticsDTO statistics) {
        if (statistics != null) {
            totalQuestionsAnswered = statistics.getPracticeQuestionCount();
            totalTimeSpentInSeconds = statistics.getTotalTimeSpentInSeconds();
        }
    }
}
