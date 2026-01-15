package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.practice.*;
import com.quizspark.quizsparkserver.dto.repositories.PageResponseDTO;
import com.quizspark.quizsparkserver.services.PracticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
            @RequestParam(defaultValue = "true") boolean revealAnswer,
            @RequestParam(defaultValue = "") List<String> tags,
            @RequestParam(defaultValue = "false") boolean endlessMode) {
        UUID practiceId = practiceService.startNewPractice(bankId, size, shuffle, revealAnswer, tags, endlessMode);
        HashMap<Object, Object> body = new HashMap<>();
        body.put("id", practiceId.toString());
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping("/new/anonymous")
    public ResponseEntity<PracticeDTO> startNewAnonymousPractice(
            @RequestParam String bankId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean shuffle,
            @RequestParam(defaultValue = "") List<String> tags) {
        PracticeDTO body = practiceService.startNewAnonymPractice(bankId, size, shuffle, tags);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getPractice(
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Boolean isClosed) {
        Page<PracticeDTO> page = practiceService.getUserPractice(pageable, isClosed);
        PageResponseDTO<PracticeDTO> body = new PageResponseDTO<>(page);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/id/{practiceId}")
    public ResponseEntity<PracticeDTO> getPractice(@PathVariable String practiceId) {
        PracticeDTO body = practiceService.getPractice(practiceId)
                .appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/id/{practiceId}/answer")
    public ResponseEntity<Object> answerQuestion(@PathVariable String practiceId,
            @RequestBody PracticeAnswerDTO answer) {
        PracticeAnswerResponseDTO body = practiceService.answerPracticeQuestion(
                practiceId,
                answer.getIndex(),
                answer.getAnswer(),
                answer.getSecondsToAnswer());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/id/{practiceId}/finish")
    public ResponseEntity<Object> finishPractice(@PathVariable String practiceId) {
        PracticeDTO body = practiceService.finishPractice(practiceId)
                .appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/id/{practiceId}/duplicate")
    public ResponseEntity<Object> duplicatePractice(@PathVariable String practiceId) {
        UUID newPracticeId = practiceService.duplicatePractice(practiceId);
        HashMap<Object, Object> body = new HashMap<>();
        body.put("id", newPracticeId.toString());
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping("/id/{practiceId}/fetch")
    public ResponseEntity<List<PracticeQuestionDTO>> fetchQuestions(@PathVariable String practiceId,
                                                                  @RequestBody PracticeFetchRequestDTO request) {
        List<PracticeQuestionDTO> body = practiceService.fetchNextQuestions(
                practiceId,
                request.getNumberOfQuestions(),
                request.getAiQuestions());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @DeleteMapping("/id/{practiceId}/questions/{index}")
    public ResponseEntity<Object> discardQuestion(@PathVariable String practiceId,
                                                @PathVariable int index) {
        practiceService.discardQuestion(practiceId, index);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
