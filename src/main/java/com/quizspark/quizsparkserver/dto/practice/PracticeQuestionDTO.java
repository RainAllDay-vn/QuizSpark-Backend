package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
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
    @JsonIgnore
    private PracticeQuestion practiceQuestion;
    private int index;
    private String baseQuestionId;
    private String ownerId;
    private String type;
    private List<TagDTO> tags;
    private String description;
    private List<String> choices;
    private List<String> correctAnswer;
    private List<String> userAnswer;
    private int secondsToAnswer;
    private List<QuestionCommentDTO> comments;

    public PracticeQuestionDTO(PracticeQuestion practiceQuestion) {
        this.practiceQuestion = practiceQuestion;
        this.index = practiceQuestion.getId().getIndex();
        this.baseQuestionId = practiceQuestion.getQuestion().getId().toString();
        this.ownerId = practiceQuestion.getQuestion().getQuestionBank().getUser().getId().toString();
        this.type = practiceQuestion.getQuestion().getQuestionType().toString();
        this.tags = practiceQuestion.getQuestion().getTags().stream().map(TagDTO::new).sorted().toList();
        this.description = practiceQuestion.getQuestion().getDescription();
        this.userAnswer = practiceQuestion.getUserAnswer();
        this.secondsToAnswer = practiceQuestion.getSecondsToAnswer() != null ? practiceQuestion.getSecondsToAnswer()
                : 0;
        applyShuffle();
    }

    private void applyShuffle() {
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

            // Map userAnswer from base indices to shuffled indices
            if (this.userAnswer != null) {
                List<String> shuffledUserAnswer = new ArrayList<>();
                for (String ans : this.userAnswer) {
                    try {
                        int baseIdx = Integer.parseInt(ans);
                        int shuffledIdx = shuffleMapping.indexOf(baseIdx);
                        if (shuffledIdx != -1) {
                            shuffledUserAnswer.add(String.valueOf(shuffledIdx));
                        } else {
                            shuffledUserAnswer.add(ans);
                        }
                    } catch (NumberFormatException e) {
                        shuffledUserAnswer.add(ans);
                    }
                }
                this.userAnswer = shuffledUserAnswer;
            }
        } else {
            this.choices = originalQuestion.getChoices();
        }
    }

    public void appendCorrectAnswer() {
        this.correctAnswer = practiceQuestion.getCorrectAnswer();
    }

    public void appendComments() {
        this.comments = practiceQuestion.getQuestion()
                .getComments()
                .stream()
                .map(QuestionCommentDTO::new)
                .toList();
    }
}
