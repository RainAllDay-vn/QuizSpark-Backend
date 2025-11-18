package com.quizspark.quizsparkserver.dto.practice;

import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionStatisticDTO implements Comparable<QuestionStatisticDTO>{
    private Question question;
    private double score = 0;

    public QuestionStatisticDTO(Question question, List<PracticeQuestion> practiceHistory) {
        this.question = question;
        practiceHistory.forEach(item -> score += getScore(item));
    }

    private double getScore(PracticeQuestion practiceQuestion){
        if (practiceQuestion.getIsCorrect() == null) return 0;
        if (practiceQuestion.getIsCorrect()) return -1;
        return 1.5;
    }

    @Override
    public int compareTo(QuestionStatisticDTO o) {
        return Double.compare(o.score, this.score);
    }
}
