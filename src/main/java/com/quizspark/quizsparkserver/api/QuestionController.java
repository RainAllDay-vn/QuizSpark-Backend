package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.questions.*;
import com.quizspark.quizsparkserver.services.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {
    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> addQuestion(@RequestParam String bankId, @RequestBody @Valid QuestionCreationDTO payload) {
        QuestionDTO body = questionService.saveQuestion(bankId, payload);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<Object> overwriteAllQuestions(
            @RequestParam String bankId,
            @RequestBody @Valid List<QuestionCreationDTO> payload
    ){
        List<QuestionDTO> body = questionService.overwriteQuestions(bankId, payload);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PutMapping("single/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable String questionId, @RequestBody @Valid QuestionUpdateDTO payload) {
        QuestionDTO body = questionService.updateQuestion(questionId, payload);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @DeleteMapping("single/{questionId}")
    public ResponseEntity<QuestionDTO> deleteQuestion(@PathVariable String questionId) {
        questionService.deleteQuestion(questionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("single/{questionId}/comments")
    public ResponseEntity<List<QuestionCommentDTO>> getComments(@PathVariable String questionId) {
        List<QuestionCommentDTO> body = questionService.getComments(questionId);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/single/{questionId}/comments")
    public ResponseEntity<QuestionCommentDTO> addComment(@PathVariable String questionId, @RequestBody @Valid QuestionCommentCreationDTO payload) {
        QuestionCommentDTO body = questionService.addComment(questionId, payload);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }
}
