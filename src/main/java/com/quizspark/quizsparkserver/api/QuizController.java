package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizspark.quizsparkserver.models.Quiz;
import com.quizspark.quizsparkserver.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {
    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<Object> getQuizzes(@RequestParam String collectionId) {
        List<Quiz> quizzes = quizService.getQuizByCollection(collectionId);
        if (quizzes == null) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(quizzes, HttpStatus.OK);
    }
}
