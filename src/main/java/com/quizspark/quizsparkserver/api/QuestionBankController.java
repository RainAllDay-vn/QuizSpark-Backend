package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.banks.QuestionBankCreationDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankUpdateDTO;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
public class QuestionBankController {
    private final QuestionService questionService;

    @Autowired
    public QuestionBankController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/public")
    public ResponseEntity<List<QuestionBankDTO>> getPublicQuestionBanks() {
        List<QuestionBankDTO> body = questionService.getPublicQuestionBanks();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<QuestionBankDTO>> getUserQuestionBanks(@AuthenticationPrincipal User user) {
        List<QuestionBankDTO> body = new ArrayList<>(questionService.getQuestionBankByUser());
        if (user.getRole().equals("ROLE_ADMIN")) {
            body.addAll(questionService.getAdminBank());
        }
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/single/{id}")
    public ResponseEntity<QuestionBankDTO> getSingleQuestionBank(@PathVariable String id) {
        QuestionBankDTO body = questionService.getQuestionBankById(id).appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPublicQuestionBank(@RequestBody @Valid QuestionBankCreationDTO creationDTO) {
        QuestionBankDTO body = questionService.savePublicQuestionBank(creationDTO);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<QuestionBankDTO> createQuestionBank(@RequestBody @Valid QuestionBankCreationDTO creationDTO) {
        QuestionBankDTO body = questionService.saveQuestionBank(creationDTO);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PutMapping("/single/{id}")
    public ResponseEntity<QuestionBankDTO> updateSingleQuestionBank(@PathVariable String id, @RequestBody @Valid QuestionBankUpdateDTO payload) {
        QuestionBankDTO body = questionService.updateQuestionBank(id, payload);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @DeleteMapping("/single/{id}")
    public ResponseEntity<Object> deleteSingleQuestionBank(@PathVariable String id) {
        questionService.deleteQuestionBank(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
