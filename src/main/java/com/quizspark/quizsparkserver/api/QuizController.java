package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Collection> optional = quizService.getCollectionById(collectionId);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Collection collection = optional.get();
        User user = (User) auth.getPrincipal();
        if (!collection.getUser().equals(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return new ResponseEntity<>(collection.getQuizzes(), HttpStatus.OK);
    }
}
