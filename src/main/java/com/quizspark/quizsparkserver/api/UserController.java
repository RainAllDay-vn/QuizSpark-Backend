package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        if (!body.containsKey("username") || !body.containsKey("email") || !body.containsKey("password")) {
            return new ResponseEntity<>("Invalid request", HttpStatus.BAD_REQUEST);
        }
        User user = new User(body.get("username"), body.get("password"), body.get("email"), encoder);
        user.toEncodedUser(encoder);
        return userService.saveUser(user)
                .map(value -> new ResponseEntity<>(value.getId().toString(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Wrong credentials", HttpStatus.BAD_REQUEST));
    }
}
