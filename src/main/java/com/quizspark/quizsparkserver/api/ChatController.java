package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.chat.*;
import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.FileRepository;
import com.quizspark.quizsparkserver.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final FileRepository fileRepository;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDTO> streamChat(@RequestBody ChatRequestDTO request) {
        return chatService.streamChat(request);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDTO>> getSessions() {
        return new ResponseEntity<>(chatService.getSessions(), HttpStatus.OK);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable UUID sessionId,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(chatService.getMessagesBySession(sessionId, user), HttpStatus.OK);
    }

    @PostMapping("/attachments")
    public ResponseEntity<UUID> uploadAttachment(@RequestParam("file") MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image attachments are supported.");
        }
        DbFile dbFile = new DbFile(file.getOriginalFilename(), contentType, file.getBytes(), null);
        dbFile = fileRepository.save(dbFile);
        return new ResponseEntity<>(dbFile.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/models")
    public ResponseEntity<List<ModelDTO>> getModels() {
        return new ResponseEntity<>(chatService.getAvailableModels(), HttpStatus.OK);
    }
}
