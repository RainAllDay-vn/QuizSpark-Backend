package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class TestService {
    HashMap<UUID, Test> testMap = new HashMap<>();
    private final QuestionService questionService;

    @Autowired
    public TestService(QuestionService questionService) {
        this.questionService = questionService;
    }

    public void createTest(UUID userId, String questionBankId) {
        QuestionBank questionBank = questionService.getQuestionBankById(questionBankId);
        testMap.put(userId, new Test(questionBank));
    }

    public List<String> getQuestions(UUID userId) {
        Test test = testMap.get(userId);
        return test.getQuestions();
    }

    public List<List<String>> getChoices(UUID userId) {
        Test test = testMap.get(userId);
        return test.getChoices();
    }

    public int submitAnswer(UUID userId, int questionNumber, int answer) {
        Test test = testMap.get(userId);
        if (test == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found");
        return test.submitAnswer(questionNumber, answer);
    }
}
