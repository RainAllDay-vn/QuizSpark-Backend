package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.QuestionBankCreationDTO;
import com.quizspark.quizsparkserver.dto.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.QuestionBankUpdateDTO;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuestionService;
import jakarta.validation.Valid;
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
    private final QuestionService questionService;

    @Autowired
    public QuestionBankController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/public")
    public ResponseEntity<List<QuestionBankDTO>> getPublicQuestionBanks() {
        List<QuestionBank> publicQuestionBanks = questionService.getPublicQuestionBanks();
        List<QuestionBankDTO> body = publicQuestionBanks.stream().map(QuestionBankDTO::new).toList();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<QuestionBankDTO>> getUserQuestionBanks() {
        List<QuestionBank> questionBanks = questionService.getQuestionBankByUser();
        List<QuestionBankDTO> body = questionBanks.stream().map(QuestionBankDTO::new).toList();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/single/{id}")
    public ResponseEntity<QuestionBankDTO> getSingleQuestionBank(@PathVariable String id) {
        QuestionBank bank = questionService.getQuestionBankById(id);
        QuestionBankDTO body = new QuestionBankDTO(bank).appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPublicQuestionBank(@RequestBody @Valid QuestionBankCreationDTO creationDTO) {
        QuestionBank bank = creationDTO.toQuestionBank(null);
        questionService.saveQuestionBank(bank);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Object> createQuestionBank(@AuthenticationPrincipal User user, @RequestBody @Valid QuestionBankCreationDTO creationDTO) {
        QuestionBank bank = creationDTO.toQuestionBank(user);
        questionService.saveQuestionBank(bank);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/single/{id}")
    public ResponseEntity<QuestionBankDTO> updateSingleQuestionBank(@PathVariable String id, @RequestBody @Valid QuestionBankUpdateDTO payload) {
        QuestionBank bank = questionService.getQuestionBankById(id);
        payload.updateQuestionBank(bank);
        questionService.saveQuestionBank(bank);
        QuestionBankDTO body = new QuestionBankDTO(bank).appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
