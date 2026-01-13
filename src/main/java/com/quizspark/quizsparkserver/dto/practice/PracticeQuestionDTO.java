package com.quizspark.quizsparkserver.dto.practice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeQuestionDTO {
    @JsonIgnore
    private PracticeQuestion practiceQuestion;
    private int index;
    private String baseQuestionId;
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
        this.baseQuestionId = practiceQuestion.getBaseQuestion().getId().toString();
        this.type = practiceQuestion.getQuestionType().toString();
        this.tags = practiceQuestion.getTags().stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedList::new));
        if (practiceQuestion.getBaseQuestion() == null) {
            this.tags.add(0, new TagDTO("OUTDATED"));
        }
        this.description = practiceQuestion.getDescription();
        this.choices = practiceQuestion.getChoices();
        this.userAnswer = practiceQuestion.getUserAnswer();
        this.secondsToAnswer = practiceQuestion.getSecondsToAnswer() != null
                ? practiceQuestion.getSecondsToAnswer()
                : 0;
    }

    public void appendCorrectAnswer() {
        this.correctAnswer = practiceQuestion.getAnswer();
    }

    public void appendComments() {
        if (practiceQuestion.getBaseQuestion() != null) {
            this.comments = practiceQuestion.getBaseQuestion()
                    .getComments()
                    .stream()
                    .map(QuestionCommentDTO::new)
                    .toList();
        } else {
            this.comments = Collections.emptyList();
        }
    }
}
