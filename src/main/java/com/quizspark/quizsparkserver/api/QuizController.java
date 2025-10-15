package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Quiz;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
        Collection collection = getCollection(collectionId, (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return new ResponseEntity<>(collection.getQuizzes(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> addQuiz(@RequestParam String collectionId, @RequestBody Quiz quiz) {
        Collection collection = getCollection(collectionId, (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        collection.getQuizzes().add(quiz);
        quiz.setCollection(collection);
        quizService.saveCollection(collection);
        quizService.saveQuiz(quiz);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private Collection getCollection(String collectionId, User user) {
        Optional<Collection> optional = quizService.getCollectionById(collectionId);
        if (optional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        Collection collection = optional.get();
        if(user.getRole().equals("ADMIN")) return collection;
        if(!collection.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this collection");
        return collection;
    }
}
