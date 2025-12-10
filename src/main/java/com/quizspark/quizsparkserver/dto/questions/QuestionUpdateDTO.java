package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.quizspark.quizsparkserver.dto.questions.ChoicesAndAnswerProcessor.*;
import static com.quizspark.quizsparkserver.dto.questions.ChoicesAndAnswerProcessor.processOpenAnswerQuestion;

@Setter
@Getter
public class QuestionUpdateDTO{
    private String questionType;

    private List<String> tags;

    private String description;

    private List<String> answer;

    private List<String> choices;

    public void updateQuestion(Question question) throws IllegalArgumentException {
        if (questionType!=null) {
            question.setQuestionType(Question.QuestionType.valueOf(questionType));
        }
        if (choices!=null || answer!=null) {
            switch (question.getQuestionType()){
                case SINGLE_ANSWER -> processSingleAnswerQuestion(question, choices, answer);
                case MULTIPLE_ANSWER -> processMultipleAnswerQuestion(question, choices, answer);
                case FILL_THE_BLANK -> processFillTheBlankQuestion(question, answer);
                case OPEN_ANSWER -> processOpenAnswerQuestion(question);
            }
        }
        if (description!=null) question.setDescription(description);
    }
}
