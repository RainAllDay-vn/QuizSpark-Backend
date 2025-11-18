package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.services.PracticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice")
public class PracticeController {
    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/new")
    public ResponseEntity<Object> startNewPractice(
            @RequestParam String bankId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean shuffle,
            @RequestParam(defaultValue = "true") boolean revealAnswer){
        UUID practiceId = practiceService.startNewPractice(bankId, size, shuffle, revealAnswer);
        HashMap<Object, Object> body = new HashMap<>();
        body.put("id", practiceId.toString());
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }
}
