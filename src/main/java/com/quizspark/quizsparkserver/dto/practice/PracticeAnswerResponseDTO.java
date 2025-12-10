package com.quizspark.quizsparkserver.dto.practice;

import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PracticeAnswerResponseDTO {
    public List<String> correctAnswer;
    public List<QuestionCommentDTO> questionComments;
}
