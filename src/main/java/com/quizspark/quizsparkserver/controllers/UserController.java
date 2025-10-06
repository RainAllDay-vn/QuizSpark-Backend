package com.quizspark.quizsparkserver.controllers;

import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
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
    public ResponseEntity<String> login(@RequestBody String username, @RequestBody String password) {
        return userService.getUser(username, password)
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
