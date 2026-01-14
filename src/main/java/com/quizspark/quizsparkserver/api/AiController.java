package com.quizspark.quizsparkserver.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quizspark.quizsparkserver.dto.ai.AiResponseDTO;
import com.quizspark.quizsparkserver.services.AiFileProcessService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiFileProcessService aiFileProcessService;

    public AiController(AiFileProcessService aiFileProcessService) {
        this.aiFileProcessService = aiFileProcessService;
    }

    @PostMapping(value = "/parse/file/{fileId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiResponseDTO> parseFile(@PathVariable String fileId) {
        return aiFileProcessService.extractQuestion(fileId);
    }

    @PostMapping(value = "/generate/bank/{bankId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiResponseDTO> generateQuestion(@PathVariable String bankId) throws JsonProcessingException {
        return aiFileProcessService.generateQuestionFromPracticeHistory(bankId);
    }

    @PostMapping(value = "/extract/file/{fileId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiResponseDTO> extractFile(@PathVariable String fileId) {
        return aiFileProcessService.extractContent(fileId);
    }
}
