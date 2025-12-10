package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.QuestionComment;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class QuestionCommentDTO {
    private String id;
    private String user;
    private String comment;
    private Date date;
    private boolean isAnswer;
    private boolean isAI;

    public QuestionCommentDTO(QuestionComment comment) {
        this.id = comment.getId().toString();
        this.user = comment.getUser().getUsername();
        this.comment = comment.getComment();
        this.date = comment.getDate();
        this.isAnswer = comment.isAnswer();
        this.isAI = comment.isAI();
    }
}
