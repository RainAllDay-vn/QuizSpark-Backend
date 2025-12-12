package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.questions.QuestionCommentCreationDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentUpdateDTO;
import com.quizspark.quizsparkserver.services.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions/comments")
public class QuestionCommentController {
    private final QuestionService questionService;

    public QuestionCommentController(QuestionService questionService) {
        this.questionService = questionService;
    }


    @GetMapping
    public ResponseEntity<List<QuestionCommentDTO>> getComments(@RequestParam String questionId) {
        List<QuestionCommentDTO> body = questionService.getComments(questionId);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<QuestionCommentDTO> addComment(@RequestParam String questionId, @RequestBody @Valid QuestionCommentCreationDTO payload) {
        QuestionCommentDTO body = questionService.addComment(questionId, payload);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PutMapping("/single/{commentId}")
    public ResponseEntity<QuestionCommentDTO> updateComment(@PathVariable String commentId, @RequestBody @Valid QuestionCommentUpdateDTO payload) {
        QuestionCommentDTO body = questionService.updateComment(commentId, payload);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
