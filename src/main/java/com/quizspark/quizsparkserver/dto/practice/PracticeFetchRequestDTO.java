package com.quizspark.quizsparkserver.dto.practice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeFetchRequestDTO {
    private int numberOfQuestions = 5;
    private int aiQuestions = 0;
}
