package com.quizspark.quizsparkserver.dto.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDTO {
    private UUID id;
    private UUID questionBankId;
    private String questionType;
    private List<TagDTO> tags;
    private String description;
    private List<String> answer;
    private List<String> choices;
    private Integer totalAttempts;
    private Integer correctAttempts;

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.questionBankId = question.getQuestionBank().getId();
        this.questionType = question.getQuestionType().toString();
        this.tags = question.getTags().stream().map(TagDTO::new).sorted().toList();
        this.description = question.getDescription();
        this.answer = question.getAnswer();
        this.choices = question.getChoices();
    }

    public QuestionDTO appendStatistics(List<PracticeQuestion> practiceQuestions) {
        this.totalAttempts = practiceQuestions.size();
        this.correctAttempts = (int) practiceQuestions.stream()
                .map(PracticeQuestion::getIsCorrect)
                .filter(isCorrect -> Boolean.TRUE.equals(isCorrect))
                .count();
        return this;
    }
}
