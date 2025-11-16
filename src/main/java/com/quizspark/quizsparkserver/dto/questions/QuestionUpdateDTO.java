package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class QuestionUpdateDTO {
    private String description;
    private List<String> choices;
    private int answer;
    private List<String> tags;
    
    public void updateQuestion(Question question) throws IllegalArgumentException{
        if (answer < 0 || answer >= choices.size()) {
            throw new IllegalArgumentException("Answer is outside of choices' range");
        }
        question.setTags(tags);
        question.setDescription(description);
        question.setAnswer(answer);
        question.setChoices(choices);
    }
}
