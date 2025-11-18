package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeQuestionDTO {
    private int id;
    private int index;
    private List<String> tags;
    private String description;
    private List<String> choices;
    private Integer answer;
    private Integer userAnswer;

    public PracticeQuestionDTO(PracticeQuestion practiceQuestion) {
        this.id = practiceQuestion.getId();
        this.index = practiceQuestion.getIndex();
        this.tags = practiceQuestion.getQuestion().getTags();
        this.description = practiceQuestion.getQuestion().getDescription();
        applyShuffle(practiceQuestion);
        this.userAnswer = practiceQuestion.getAnswer();
    }

    private void applyShuffle(PracticeQuestion practiceQuestion){
        Question originalQuestion = practiceQuestion.getQuestion();
        Integer[] shuffleMapping = practiceQuestion.getShuffleMapping();

        // If there's shuffle mapping, apply it
        if (shuffleMapping != null && shuffleMapping.length > 1) {
            List<String> originalChoices = originalQuestion.getChoices();
            List<String> shuffledChoices = new ArrayList<>(originalChoices.size());
            for (int i = 0; i < originalChoices.size(); i++) {
                shuffledChoices.add(originalChoices.get(shuffleMapping[i]));
            }
            this.choices = shuffledChoices;

            int originalAnswer = originalQuestion.getAnswer();
            for (int i = 0; i < shuffleMapping.length; i++) {
                if (shuffleMapping[i] == originalAnswer) {
                    this.answer = i;
                    break;
                }
            }
        }
    }
}
