package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.chat.*;
import com.quizspark.quizsparkserver.services.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatservice) {
        this.chatService = chatservice;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDTO> streamChat(@Valid @RequestBody ChatRequestDTO request) {
        return chatService.streamChat(request);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDTO>> getSessions() {
        return new ResponseEntity<>(chatService.getSessions(), HttpStatus.OK);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable UUID sessionId) {
        return new ResponseEntity<>(chatService.getMessagesBySession(sessionId), HttpStatus.OK);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID sessionId) {
        chatService.deleteSession(sessionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/models")
    public ResponseEntity<List<ModelDTO>> getModels() {
        return new ResponseEntity<>(chatService.getAvailableModels(), HttpStatus.OK);
    }
}
