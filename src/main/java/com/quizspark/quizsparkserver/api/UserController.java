package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/me")
    public ResponseEntity<Object> getUserData() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String,String> response = new HashMap<>();
        response.put("username", user.getUsername());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody JsonNode body) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user != null) return new ResponseEntity<>(HttpStatus.CONFLICT);
        String username = body.get("username").asText();
        String email = body.get("email").asText();
        user = new User(username, email);
        userService.saveUser(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
