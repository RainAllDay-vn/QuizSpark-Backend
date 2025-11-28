package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.practice.PracticeAnswerDTO;
import com.quizspark.quizsparkserver.dto.practice.PracticeDTO;
import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.services.PracticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/new/anonymous")
    public ResponseEntity<PracticeDTO> startNewAnonymousPractice(
            @RequestParam String bankId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean shuffle
    ){
        Practice practice = practiceService.startNewAnonymPractice(bankId, size, shuffle);
        PracticeDTO body = new PracticeDTO(practice).appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PracticeDTO>> getPractice(){
        List<Practice> practices = practiceService.getUserPractice();
        List<PracticeDTO> body = practices.stream().map(PracticeDTO::new).toList();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/id/{practiceId}")
    public ResponseEntity<PracticeDTO> getPractice(@PathVariable String practiceId){
        Practice practice = practiceService.getPractice(practiceId);
        PracticeDTO body = new PracticeDTO(practice).appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/id/{practiceId}/answer")
    public ResponseEntity<Object> answerQuestion(@PathVariable String practiceId, @RequestBody PracticeAnswerDTO answer){
        List<String> correctAnswer = practiceService.answerPracticeQuestion(
                practiceId,
                answer.getIndex(),
                answer.getAnswer(),
                answer.getSecondsToAnswer());
        HashMap<String, Object> body = new HashMap<>();
        body.put("correctAnswer", correctAnswer);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

}
