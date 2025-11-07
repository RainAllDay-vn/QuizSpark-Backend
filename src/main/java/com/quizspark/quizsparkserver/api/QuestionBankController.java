package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuestionService;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
public class QuestionBankController {
    private final UserService userService;
    private final QuestionService questionService;

    @Autowired
    public QuestionBankController(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;
    }

    @GetMapping("/public")
    public ResponseEntity<Object> getPublicQuestionBanks() {
        List<QuestionBank> publicQuestionBanks = questionService.getPublicQuestionBanks();
        return new ResponseEntity<>(publicQuestionBanks, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<Object> getQuestionBanks(@AuthenticationPrincipal User user) {
        List<QuestionBank> questionBanks = questionService.getQuestionBankByUser(user.getId().toString());
        if (questionBanks == null) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(questionBanks, HttpStatus.OK);
    }

    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPublicQuestionBank(@RequestBody QuestionBank questionBank) {
        System.out.println(questionBank);
        questionBank = questionService.saveQuestionBank(questionBank);
        return new ResponseEntity<>(questionBank, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Object> createQuestionBank(@AuthenticationPrincipal User user, @RequestBody QuestionBank questionBank) {
        questionBank.setUser(user);
        user.getQuestionBanks().add(questionBank);
        questionService.saveQuestionBank(questionBank);
        userService.saveUser(user);
        return new ResponseEntity<>(questionBank, HttpStatus.CREATED);
    }
}
