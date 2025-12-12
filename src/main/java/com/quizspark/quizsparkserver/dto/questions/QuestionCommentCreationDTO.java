package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionComment;
import com.quizspark.quizsparkserver.models.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class QuestionCommentCreationDTO {
    @NotBlank
    private String comment;

    public QuestionComment toComment(User user, Question question) {
        QuestionComment comment = new QuestionComment();
        comment.setUser(user);
        comment.setQuestion(question);
        comment.setComment(this.comment);
        comment.setDate(new Date());
        comment.setAI(false);
        comment.setAnswer(false);
        return comment;
    }
}
