package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.UserService;
import com.quizspark.quizsparkserver.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final UserService userService;

    @Autowired
    public UserController(JwtUtil jwtUtil, PasswordEncoder encoder, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.userService = userService;
    }

    @GetMapping("/status")
    public ResponseEntity<Object> getUserDatabaseStatus() {
        return new ResponseEntity<>(userService.getUserDatabaseStatus(), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!encoder.matches(password, userDetails.getPassword())) {
            return new ResponseEntity<>("Wrong Credential", HttpStatus.UNAUTHORIZED);
        }
        String token = jwtUtil.generateToken(body.get("username"));
        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        response.put("token", token);
        return ResponseEntity.ok(response);
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
                .orElseGet(() -> new ResponseEntity<>("Bad credentials", HttpStatus.BAD_REQUEST));
    }
}
