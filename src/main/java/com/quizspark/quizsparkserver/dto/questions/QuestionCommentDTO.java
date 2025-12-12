package com.quizspark.quizsparkserver.dto.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.QuestionComment;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionCommentDTO {
    private String id;
    private String user;
    private String comment;
    private Date createdDate;
    private Date editedDate;
    private boolean isAnswer;
    private boolean isAI;

    public QuestionCommentDTO(QuestionComment comment) {
        this.id = comment.getId().toString();
        this.user = comment.getUser().getUsername();
        this.comment = comment.getComment();
        this.createdDate = comment.getCreatedDate();
        this.editedDate = comment.getEditedDate();
        this.isAnswer = comment.isAnswer();
        this.isAI = comment.isAI();
    }
}
