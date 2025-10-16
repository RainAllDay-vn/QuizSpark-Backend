package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quizspark.quizsparkserver.services.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    private final ObjectMapper mapper;
    private final TestService testService;

    @Autowired
    public TestController(ObjectMapper mapper, TestService testService) {
        this.mapper = mapper;
        this.testService = testService;
    }

    @PostMapping("/new")
    public ResponseEntity<Object> createTest(HttpSession session, @RequestParam String collectionId) {
        testService.createTest(session.getId(), collectionId);
        return new ResponseEntity<>(session.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/questions")
    public ResponseEntity<Object> getQuestions(HttpSession session) {
        System.out.println(session.getId());
        List<String> questions = testService.getQuestions(session.getId());
        List<List<String>> choices = testService.getChoices(session.getId());
        ArrayNode response = mapper.createArrayNode();
        for (int i = 0; i < questions.size(); i++) {
            ObjectNode questionNode = mapper.createObjectNode();
            questionNode.put("question", questions.get(i));
            questionNode.set("options", mapper.valueToTree(choices.get(i)));
            response.add(questionNode);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
