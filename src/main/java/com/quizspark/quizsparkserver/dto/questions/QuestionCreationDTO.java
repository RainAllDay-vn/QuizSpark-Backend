package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionBank;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.quizspark.quizsparkserver.dto.questions.ChoicesAndAnswerProcessor.*;

@Getter
@Setter
public class QuestionCreationDTO {
    @NotBlank
    private String questionType;

    private List<String> tags;

    @NotBlank(message = "Question description must not be blank")
    private String description;

    private List<String> answer;

    private List<String> choices;

    private String explanation;

    public Question toQuestion(QuestionBank bank) throws IllegalArgumentException{
        Question question = new Question();
        question.setQuestionType(Question.QuestionType.valueOf(questionType));
        switch (question.getQuestionType()){
            case SINGLE_ANSWER -> processSingleAnswerQuestion(question, choices, answer);
            case MULTIPLE_ANSWER -> processMultipleAnswerQuestion(question, choices, answer);
            case FILL_THE_BLANK -> processFillTheBlankQuestion(question, answer);
            case OPEN_ANSWER -> processOpenAnswerQuestion(question);
        }
        question.setTags(tags==null ? new ArrayList<>() : tags);
        question.setDescription(description);
        question.setQuestionBank(bank);
        if (explanation!=null && !explanation.isBlank()) question.setExplanation(explanation);
        question.setExplanation(null);
        return question;
    }
}
