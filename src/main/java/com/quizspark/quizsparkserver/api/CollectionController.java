package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuizService;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final UserService userService;
    private final QuizService quizService;

    @Autowired
    public CollectionController(UserService userService, QuizService quizService) {
        this.userService = userService;
        this.quizService = quizService;
    }

    @GetMapping("/public")
    public ResponseEntity<Object> getPublicCollections() {
        List<Collection> publicCollections = quizService.getPublicCollections();
        return new ResponseEntity<>(publicCollections, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<Object> getCollections(@AuthenticationPrincipal User user) {
        List<Collection> collections = quizService.getCollectionByUser(user.getId().toString());
        if (collections == null) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(collections, HttpStatus.OK);
    }

    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPublicCollection(@RequestBody Collection collection) {
        System.out.println(collection);
        collection = quizService.saveCollection(collection);
        return new ResponseEntity<>(collection, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Object> createCollection(@AuthenticationPrincipal User user, @RequestBody Collection collection) {
        collection.setUser(user);
        user.getCollections().add(collection);
        quizService.saveCollection(collection);
        userService.saveUser(user);
        return new ResponseEntity<>(collection, HttpStatus.CREATED);
    }
}
