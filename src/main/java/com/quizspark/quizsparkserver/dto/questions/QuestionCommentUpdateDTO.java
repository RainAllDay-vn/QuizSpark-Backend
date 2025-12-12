package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.QuestionComment;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class QuestionCommentUpdateDTO {
    private String comment;
    private Boolean isAnswer;
    private Boolean isAI;

    public void updateComment(QuestionComment questionComment) {
        if (comment!=null && !comment.equals(questionComment.getComment())) {
            questionComment.setComment(comment);
            questionComment.setEditedDate(new Date());
        }
        if (isAI!=null) {
            questionComment.setAI(isAI);
        }
        if (isAnswer!=null) {
            questionComment.setAnswer(isAnswer);
        }
    }
}
