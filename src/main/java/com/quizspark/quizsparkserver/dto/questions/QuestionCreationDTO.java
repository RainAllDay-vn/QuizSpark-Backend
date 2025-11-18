package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionBank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionCreationDTO {
    @NotNull
    private List<String> tags;
    @NotBlank
    private String description;
    @NotNull
    private int answer;
    @NotNull
    @Size(min=2)
    private List<String> choices;

    public Question toQuestion(QuestionBank bank) throws IllegalArgumentException{
        if (answer<0 || answer>=choices.size()) throw new IllegalArgumentException("Answer is out side of choices' range");
        Question question = new Question();
        question.setTags(tags);
        question.setDescription(description);
        question.setAnswer(answer);
        question.setChoices(choices);
        question.setQuestionBank(bank);
        return question;
    }
}
