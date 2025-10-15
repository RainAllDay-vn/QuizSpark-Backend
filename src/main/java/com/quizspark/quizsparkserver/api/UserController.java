package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final PasswordEncoder encoder;
    private final UserService userService;

    @Autowired
    public UserController(PasswordEncoder encoder, UserService userService) {
        this.encoder = encoder;
        this.userService = userService;
    }

    @GetMapping("/status")
    public ResponseEntity<Object> getUserDatabaseStatus() {
        return new ResponseEntity<>(userService.getUserDatabaseStatus(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.toEncodedUser(encoder);
        return userService.saveUser(user)
                .map(value -> new ResponseEntity<>(value.getId().toString(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Invalid user", HttpStatus.BAD_REQUEST));
    }
}
