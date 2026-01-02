package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.chat.ChatMessageDTO;
import com.quizspark.quizsparkserver.dto.chat.ChatRequestDTO;
import com.quizspark.quizsparkserver.dto.chat.ChatResponseDTO;
import com.quizspark.quizsparkserver.dto.chat.ChatSessionDTO;
import com.quizspark.quizsparkserver.models.ChatMessage;
import com.quizspark.quizsparkserver.models.ChatSession;
import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.ChatMessageRepository;
import com.quizspark.quizsparkserver.repositories.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FileService fileService;
    private final ChatModel chatModel;

    @Transactional
    public Flux<ChatResponseDTO> streamChat(ChatRequestDTO request) {
        User user = getAuthenticatedUser();
        ChatSession session;
        if (request.getSessionId() == null) {
            session = new ChatSession(user,
                    request.getMessage().substring(0, Math.min(request.getMessage().length(), 50)));
            session = chatSessionRepository.save(session);
        } else {
            session = chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
            if (!session.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        if (request.getIndex() != null) {
            chatMessageRepository.deleteBySessionAndMessageIndexGreaterThanEqual(session, request.getIndex());
        }

        int nextIndex = chatMessageRepository.countBySession(session).intValue();

        DbFile file = null;
        if (request.getFileId() != null) {
            file = fileService.getFileEntity(request.getFileId().toString());
        }

        ChatMessage userMessage = new ChatMessage(session, ChatMessage.MessageRole.USER, request.getMessage(),
                request.getModel(), file, nextIndex);
        chatMessageRepository.save(userMessage);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("You are a helpful educational assistant for QuizSpark."));

        // Load History
        List<ChatMessage> history = chatMessageRepository.findBySessionOrderByMessageIndexAsc(session);
        for (ChatMessage msg : history) {
            if (msg.getRole() == ChatMessage.MessageRole.USER) {
                if (msg.getFile() != null) {
                    Media media = new Media(MimeTypeUtils.parseMimeType(msg.getFile().getFileType()),
                            new ByteArrayResource(msg.getFile().getData()));
                    messages.add(new UserMessage(msg.getContent(), List.of(media)));
                } else {
                    messages.add(new UserMessage(msg.getContent()));
                }
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        final UUID sessionId = session.getId();
        final String model = request.getModel() != null ? request.getModel() : "deepseek-ai/DeepSeek-V3.2-TEE";
        final int assistantIndex = nextIndex + 1;

        Prompt prompt = new Prompt(messages, OpenAiChatOptions.builder().model(model).build());

        StringBuilder fullAssistantResponse = new StringBuilder();

        final ChatSession finalSession = session;
        return chatModel.stream(prompt)
                .map(response -> {
                    String content = (response.getResult() != null && response.getResult().getOutput() != null)
                            ? response.getResult().getOutput().getContent()
                            : "";
                    fullAssistantResponse.append(content);
                    return ChatResponseDTO.builder()
                            .chunk(content)
                            .sessionId(sessionId)
                            .role(ChatMessage.MessageRole.ASSISTANT)
                            .status("data")
                            .build();
                })
                .filter(resp -> !resp.getChunk().isEmpty())
                .doOnComplete(() -> {
                    ChatMessage assistantMsg = new ChatMessage(finalSession, ChatMessage.MessageRole.ASSISTANT,
                            fullAssistantResponse.toString(),
                            model, null, assistantIndex);
                    chatMessageRepository.save(assistantMsg);
                })
                .concatWith(Flux.just(ChatResponseDTO.builder().status("finish").sessionId(sessionId).build()));
    }

    public List<ChatSessionDTO> getSessions() {
        return chatSessionRepository.findByUserOrderByCreatedAtDesc(getAuthenticatedUser())
                .stream()
                .map(this::mapToChatSessionDTO)
                .collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getMessagesBySession(UUID sessionId) {
        User user = getAuthenticatedUser();
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return chatMessageRepository.findBySessionOrderByMessageIndexAsc(session)
                .stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());
    }

    private ChatSessionDTO mapToChatSessionDTO(ChatSession session) {
        return ChatSessionDTO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .createdAt(session.getCreatedAt())
                .build();
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .model(message.getModel())
                .messageIndex(message.getMessageIndex())
                .fileId(message.getFile() != null ? message.getFile().getId() : null)
                .createdAt(message.getCreatedAt())
                .build();
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
