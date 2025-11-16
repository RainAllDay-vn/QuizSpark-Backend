package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionAnswerLog;
import com.quizspark.quizsparkserver.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class QuestionAnswerLogDTO {
    private String questionId;
    private int answer;

    public boolean validateAnswer(Question question) {
        return answer >= 0 && answer < question.getChoices().size();
    }

    public QuestionAnswerLog toQuestionAnswerLog(User user, Question question) {
        QuestionAnswerLog log = new QuestionAnswerLog();
        log.setDate(new Date());
        log.setUser(user);
        log.setQuestion(question);
        log.setAnswer(answer);
        log.setCorrect(answer == question.getAnswer());
        return log;
    }
}
