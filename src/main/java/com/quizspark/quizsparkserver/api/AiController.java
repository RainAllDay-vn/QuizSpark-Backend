package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.AiResponseDTO;
import com.quizspark.quizsparkserver.services.AiService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/parse/{fileId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiResponseDTO> parseFile(@PathVariable String fileId) {
        return aiService.extractQuestion(fileId);
    }
}
