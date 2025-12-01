package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeQuestionDTO {
    @JsonIgnore
    private PracticeQuestion practiceQuestion;
    private int index;
    private String type;
    private List<String> tags;
    private String description;
    private List<String> choices;
    private List<String> correctAnswer;
    private List<String> userAnswer;

    public PracticeQuestionDTO(PracticeQuestion practiceQuestion) {
        this.practiceQuestion = practiceQuestion;
        this.index = practiceQuestion.getId().getIndex();
        this.type = practiceQuestion.getQuestion().getQuestionType().toString();
        this.tags = practiceQuestion.getQuestion().getTags().stream().map(Tag::getName).toList();
        this.description = practiceQuestion.getQuestion().getDescription();
        this.userAnswer = practiceQuestion.getUserAnswer();
        applyShuffle();
    }

    private void applyShuffle(){
        Question originalQuestion = practiceQuestion.getQuestion();
        List<Integer> shuffleMapping = practiceQuestion.getShuffleMapping();

        // If there's shuffle mapping, apply it
        if (shuffleMapping != null && shuffleMapping.size() > 1) {
            List<String> originalChoices = originalQuestion.getChoices();
            List<String> shuffledChoices = new ArrayList<>(originalChoices.size());
            for (int i = 0; i < originalChoices.size(); i++) {
                shuffledChoices.add(originalChoices.get(shuffleMapping.get(i)));
            }
            this.choices = shuffledChoices;
        } else {
            this.choices = originalQuestion.getChoices();
        }
    }

    public void appendCorrectAnswer() {
        this.correctAnswer = practiceQuestion.getCorrectAnswer();
    }
}
