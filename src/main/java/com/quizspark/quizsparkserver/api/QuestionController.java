package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.daos.QuestionAnswerLogDAO;
import com.quizspark.quizsparkserver.models.QuestionAnswerLog;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {
    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<Object> getQuestions(@RequestParam String bankId) {
        QuestionBank questionBank = questionService.getQuestionBankById(bankId);
        return new ResponseEntity<>(questionBank.getQuestions(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> addQuestion(@RequestParam String bankId, @RequestBody Question question) {
        QuestionBank questionBank = questionService.getQuestionBankById(bankId);
        User collectionUser = questionBank.getUser();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (collectionUser==null && !user.getRole().equals("ROLE_ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        questionBank.getQuestions().add(question);
        question.setQuestionBank(questionBank);
        questionService.saveQuestionBank(questionBank);
        questionService.saveQuestion(question);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/log")
    public ResponseEntity<String> log(@RequestBody QuestionAnswerLogDAO logDAO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Question question = questionService.getQuestionById(logDAO.getQuestionId());
        if (!logDAO.validateAnswer(question)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        QuestionAnswerLog log = logDAO.toQuestionAnswerLog(user, question);
        questionService.saveQuestionAnswerLog(log);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
