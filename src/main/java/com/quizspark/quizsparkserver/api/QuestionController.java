package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.questions.QuestionCreationDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionUpdateDTO;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.services.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        QuestionBank questionBank = questionService.getQuestionBankById(bankId);
        Question question = payload.toQuestion(questionBank);
        questionService.saveQuestion(question);
        QuestionDTO body = new QuestionDTO(question);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable String questionId, @RequestBody @Valid QuestionUpdateDTO payload) {
        Question question = questionService.getQuestionById(questionId);
        payload.updateQuestion(question);
        questionService.saveQuestion(question);
        QuestionDTO body = new QuestionDTO(question);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> deleteQuestion(@PathVariable String questionId) {
        Question question = questionService.getQuestionById(questionId);
        questionService.deleteQuestion(question);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
