package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public int getTestSize(String sessionId) {
        return testMap.get(sessionId).getSize();
    }

    public List<String> getQuestionAndChoices(String sessionId, int questionNumber) {
        Test test = testMap.get(sessionId);
        List<String> result = new LinkedList<>();
        result.add(test.getQuestion(questionNumber));
        result.addAll(test.getChoices(questionNumber));
        return result;
    }

    public int submitAnswer(String sessionId, int questionNumber, int answer) {
        Test test = testMap.get(sessionId);
        if (test == null) throw new RuntimeException("Test not found");
        return test.submitAnswer(questionNumber, answer);
    }
}
