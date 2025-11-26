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
    private int index;
    private String type;
    private List<String> tags;
    private String description;
    private List<String> choices;
    private List<String> answer;
    private List<String> userAnswer;

    public PracticeQuestionDTO(PracticeQuestion practiceQuestion) {
        this.index = practiceQuestion.getIndex();
        this.type = practiceQuestion.getQuestion().getQuestionType().toString();
        this.tags = practiceQuestion.getQuestion().getTags();
        this.description = practiceQuestion.getQuestion().getDescription();
        applyShuffle(practiceQuestion);
        this.userAnswer = practiceQuestion.getUserAnswer();
    }

    private void applyShuffle(PracticeQuestion practiceQuestion){
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

            switch (originalQuestion.getQuestionType()){
                case SINGLE_ANSWER:
                case MULTIPLE_ANSWER:
                    List<Integer> originalAnswer = originalQuestion.getAnswer()
                            .stream()
                            .map(Integer::parseInt)
                            .toList();
                    this.answer = new ArrayList<>(originalAnswer.size());
                    for (int i = 0; i < shuffleMapping.size(); i++) {
                        if (originalAnswer.contains(shuffleMapping.get(i))) {
                            this.answer.add(String.valueOf(i));
                        }
                    }
                    break;
                case FILL_THE_BLANK:
                    this.answer = new ArrayList<>(originalChoices.size());
                    for (int i = 0; i < originalChoices.size(); i++) {
                        this.answer.add(originalChoices.get(shuffleMapping.get(i)));
                    }
                    break;
                default:
                    this.answer = originalQuestion.getAnswer();
            }
        } else {
            this.choices = originalQuestion.getChoices();
            this.answer = originalQuestion.getAnswer();
        }
    }
}
