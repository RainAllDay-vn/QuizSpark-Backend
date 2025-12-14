package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.agent.ChatMessageDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final WebClient webClient;

    public AgentController(WebClient.Builder builder, @Value("${agent.service.url:http://localhost:8000}") String agentUrl) {
        this.webClient = builder
                .baseUrl(agentUrl)
                .build();
    }

    @PostMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamResponse(@RequestBody @Valid List<ChatMessageDTO> payload) {
        return webClient.post()
                .uri("/agent/stream/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(event -> event.data() != null ? event.data() : "");
    }
}
