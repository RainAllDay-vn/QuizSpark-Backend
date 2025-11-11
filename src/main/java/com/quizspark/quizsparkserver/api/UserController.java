package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.daos.UserRegistrationDAO;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import jakarta.validation.Valid;
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
        if (user.getRole().equals("ROLE_NEW")) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Map<String,String> response = new HashMap<>();
        response.put("username", user.getUsername());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDAO userRegistration) {
        User tempUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = tempUser.getEmail();
        User newUser = userRegistration.toUser(email);
        userService.saveUser(newUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
