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
    HashMap<UUID, Test> testMap = new HashMap<>();
    private final QuizService quizService;

    @Autowired
    public TestService(QuizService quizService) {
        this.quizService = quizService;
    }

    public void createTest(UUID userId, String collectionId) {
        Collection collection = quizService.getCollectionById(collectionId);
        testMap.put(userId, new Test(collection));
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
