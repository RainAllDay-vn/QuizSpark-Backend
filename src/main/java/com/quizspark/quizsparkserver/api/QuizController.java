package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Quiz;
import com.quizspark.quizsparkserver.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Collection collection = quizService.getCollectionById(collectionId);
        return new ResponseEntity<>(collection.getQuizzes(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> addQuiz(@RequestParam String collectionId, @RequestBody Quiz quiz) {
        Collection collection = quizService.getCollectionById(collectionId);
        collection.getQuizzes().add(quiz);
        quiz.setCollection(collection);
        quizService.saveCollection(collection);
        quizService.saveQuiz(quiz);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
