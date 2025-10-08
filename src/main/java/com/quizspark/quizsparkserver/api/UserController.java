package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/status")
    public ResponseEntity<Object> getUserDatabaseStatus() {
        return new ResponseEntity<>(userService.getUserDatabaseStatus(), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody JsonNode node) {
        if (!node.has("username") && !node.has("password")) {
            return new ResponseEntity<>("Missing username or password",HttpStatus.BAD_REQUEST);
        }
        return userService.getUser(node.get("username").asText(), node.get("password").asText())
                .map(value -> new ResponseEntity<>(value.getId().toString(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("There is no such user", HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return userService.saveUser(user)
                .map(value -> new ResponseEntity<>(value.getId().toString(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Invalid user", HttpStatus.BAD_REQUEST));
    }
}
