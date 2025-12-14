package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.agent.ChatMessageDTO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    @PostMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamResponse(@RequestBody @Valid List<ChatMessageDTO> payload) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        executor.execute(() -> {
            try {
                String response = "This is a simulated response";
                // Send response one character at a time
                for (int i = 0; i < response.length(); i++) {
                    emitter.send(response.charAt(i));
                    Thread.sleep(100); // Small delay to simulate streaming
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
}
