package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class TestService {
    HashMap<String, Test> testMap = new HashMap<>();
    private final QuizService quizService;

    @Autowired
    public TestService(QuizService quizService) {
        this.quizService = quizService;
    }

    public void createTest(String sessionId, String collectionId) {
        Collection collection = quizService.getCollectionById(collectionId);
        testMap.put(sessionId, new Test(collection));
    }

    public List<String> getQuestions(String sessionId) {
        Test test = testMap.get(sessionId);
        return test.getQuestions();
    }

    public List<List<String>> getChoices(String sessionId) {
        Test test = testMap.get(sessionId);
        return test.getChoices();
    }

    public int submitAnswer(String sessionId, int questionNumber, int answer) {
        Test test = testMap.get(sessionId);
        if (test == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found");
        return test.submitAnswer(questionNumber, answer);
    }
}
