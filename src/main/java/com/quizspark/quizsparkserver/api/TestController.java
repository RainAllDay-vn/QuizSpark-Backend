package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Object> createTest(@AuthenticationPrincipal User user, @RequestParam String collectionId) {
        testService.createTest(user.getId(), collectionId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/questions")
    public ResponseEntity<Object> getQuestions(@AuthenticationPrincipal User user) {
        List<String> questions = testService.getQuestions(user.getId());
        List<List<String>> choices = testService.getChoices(user.getId());
        ArrayNode response = mapper.createArrayNode();
        for (int i = 0; i < questions.size(); i++) {
            ObjectNode questionNode = mapper.createObjectNode();
            questionNode.put("question", questions.get(i));
            questionNode.set("options", mapper.valueToTree(choices.get(i)));
            response.add(questionNode);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/answer")
    public ResponseEntity<Object> submitAnswer(
            @AuthenticationPrincipal User user,
            @RequestParam int questionNumber,
            @RequestBody JsonNode body
    ) {
        ObjectNode response = mapper.createObjectNode();
        int choice = body.get("choice").asInt();
        int answer = testService.submitAnswer(user.getId(), questionNumber, choice);
        if (answer == -1) return new ResponseEntity<>("Designated time for the text has over", HttpStatus.BAD_REQUEST);
        response.put("correct", choice==answer);
        response.put("answer", answer);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
