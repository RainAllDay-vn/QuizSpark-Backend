package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.chat.*;
import com.quizspark.quizsparkserver.dto.chat.ModelDTO.ModelCharacteristic;
import com.quizspark.quizsparkserver.models.*;
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

    private static final List<ModelDTO> AVAILABLE_MODELS = List.of(
            // DeepSeek V3.2 (Released Dec 2025) - Supports Reasoning/Thinking Mode
            new ModelDTO("deepseek-v3.2", "deepseek-ai/DeepSeek-V3.2-TEE", "DeepSeek V3.2 TEE",
                    List.of(ModelCharacteristic.REASONING)),
            // Qwen 3 235B (Released July 2025) - Features "Thinking Mode" and dual-mode
            // switching
            new ModelDTO("qwen-3-235b", "Qwen/Qwen3-235B-A22B-Instruct-2507-TEE", "Qwen 3 235B TEE",
                    List.of(ModelCharacteristic.REASONING)),
            // GLM 4.7 (Released late 2025) - Features "Interleaved Thinking"
            new ModelDTO("glm-4.7", "zai-org/GLM-4.7-TEE", "GLM 4.7 TEE",
                    List.of(ModelCharacteristic.REASONING)),
            // Kimi K2 Thinking (Released Nov 2025) - Native Thinking Agent
            new ModelDTO("kimi-k2", "moonshotai/Kimi-K2-Thinking-TEE", "Kimi K2 Thinking TEE",
                    List.of(ModelCharacteristic.REASONING)),
            // Hermes 4 405B (Released late 2025) - Hybrid Reasoning capabilities
            new ModelDTO("hermes-4", "NousResearch/Hermes-4-405B-FP8-TEE", "Hermes 4 405B TEE",
                    List.of(ModelCharacteristic.REASONING)),
            // MiniMax M2.1 (Released Dec 2025) - "Advanced Interleaved Thinking"
            new ModelDTO("minimax-m2.1", "MiniMaxAI/MiniMax-M2.1-TEE", "MiniMax M2.1 TEE",
                    List.of(ModelCharacteristic.REASONING)),
            // Qwen 3 VL - Vision Language Model
            new ModelDTO("qwen-3-vl", "Qwen/Qwen3-VL-235B-A22B-Instruct", "Qwen 3 VL",
                    List.of(ModelCharacteristic.VISION)),
            // GLM 4.6V - Vision Model (Predecessor to 4.7 text)
            new ModelDTO("glm-4.6v", "zai-org/GLM-4.6V", "GLM 4.6V",
                    List.of(ModelCharacteristic.VISION)),
            // DeepSeek V3.2 Speciale - "Maxed-out reasoning capabilities"
            new ModelDTO("deepseek-v3.2-speciale", "deepseek-ai/DeepSeek-V3.2-Speciale-TEE",
                    "DeepSeek V3.2 Speciale TEE",
                    List.of(ModelCharacteristic.REASONING)));

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

    public List<ModelDTO> getAvailableModels() {
        return AVAILABLE_MODELS;
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
