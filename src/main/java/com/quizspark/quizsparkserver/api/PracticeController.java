package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.practice.PracticeDTO;
import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.services.PracticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/practice")
public class PracticeController {
    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/new")
    public ResponseEntity<PracticeDTO> startNewPractice(
            @RequestParam String bankId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean shuffle,
            @RequestParam(defaultValue = "true") boolean revealAnswer){
        Practice practice = practiceService.getNewPractice(bankId, size, shuffle);
        PracticeDTO body = new PracticeDTO(practice, revealAnswer);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }
}
