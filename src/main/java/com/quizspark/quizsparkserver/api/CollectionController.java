package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizspark.quizsparkserver.models.Collection;
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
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final QuizService quizService;

    @Autowired
    public CollectionController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<Object> getCollections(@RequestParam String userId) {
        List<Collection> collections = quizService.getCollectionByUser(userId);
        if (collections == null) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(collections, HttpStatus.OK);
    }
}
