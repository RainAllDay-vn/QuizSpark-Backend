package com.quizspark.quizsparkserver.dto.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeStatisticsDTO {
    private long practiceQuestionCount;
    private long totalTimeSpentInSeconds;
}
