package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.chat.*;
import com.quizspark.quizsparkserver.dto.chat.ModelDTO.ModelCharacteristic;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.ChatMessageRepository;
import com.quizspark.quizsparkserver.repositories.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
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
@Slf4j
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

        private static final String DEFAULT_MODEL = "deepseek-ai/DeepSeek-V3.2-TEE";
        private static final String SYSTEM_PROMPT = "You are a helpful educational assistant for QuizSpark.";

        @Transactional
        public Flux<ChatResponseDTO> streamChat(ChatRequestDTO request) {
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                ChatSession session = getOrCreateSession(request, user);
                handleMessageTruncation(session, request.getIndex());
                int nextIndex = chatMessageRepository.countBySession(session).intValue();
                saveUserMessage(request, session, nextIndex);
                return executeChatStream(session, request.getModel(), nextIndex + 1);
        }

        private ChatSession getOrCreateSession(ChatRequestDTO request, User user) {
                if (request.getSessionId() == null) {
                        String title = request.getMessage().substring(0, Math.min(request.getMessage().length(), 50));
                        return chatSessionRepository.save(new ChatSession(user, title));
                }
                ChatSession session = chatSessionRepository.findById(request.getSessionId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Session not found"));
                if (!session.getUser().getId().equals(user.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
                }
                return session;
        }

        private void handleMessageTruncation(ChatSession session, Integer index) {
                if (index != null) {
                        chatMessageRepository.deleteBySessionAndMessageIndexGreaterThanEqual(session, index);
                }
        }

        private void saveUserMessage(ChatRequestDTO request, ChatSession session, int index) {
                DbFile file = request.getFileId() != null ? fileService.getFileEntity(request.getFileId().toString())
                                : null;
                ChatMessage userMessage = new ChatMessage(session, ChatMessage.MessageRole.USER, request.getMessage(),
                                request.getModel(), file, index);
                chatMessageRepository.save(userMessage);
        }

        private Flux<ChatResponseDTO> executeChatStream(ChatSession session, String modelName, int assistantIndex) {
                String model = modelName != null ? modelName : DEFAULT_MODEL;
                UUID sessionId = session.getId();
                Prompt prompt = createPrompt(session, model);
                StringBuilder fullResponse = new StringBuilder();

                return chatModel.stream(prompt)
                                .map(response -> {
                                        String content = extractContent(response);
                                        fullResponse.append(content);
                                        return createChatResponse(content, sessionId, "data");
                                })
                                .filter(resp -> resp.getChunk() != null && !resp.getChunk().isEmpty())
                                .doOnComplete(() -> {
                                        if (fullResponse.length() > 0) {
                                                saveAssistantMessage(session, fullResponse.toString(), model,
                                                                assistantIndex);
                                        }
                                })
                                .startWith(Flux.just(createChatResponse("", sessionId, "start")))
                                .concatWith(Flux.just(createChatResponse("", sessionId, "finish")))
                                .onErrorResume(e -> {
                                        log.error("AI Provider error for session {}: {}", sessionId, e.getMessage(), e);
                                        return Flux.just(createChatResponse(
                                                        "Error: The AI provider is currently unavailable. Please try again later.",
                                                        sessionId, "error"));
                                });
        }

        private Prompt createPrompt(ChatSession session, String model) {
                List<Message> messages = new ArrayList<>();
                messages.add(new SystemMessage(SYSTEM_PROMPT));
                chatMessageRepository.findBySessionOrderByMessageIndexAsc(session).stream()
                                .map(this::convertToAiMessage)
                                .forEach(messages::add);
                return new Prompt(messages, OpenAiChatOptions.builder().model(model).build());
        }

        private Message convertToAiMessage(ChatMessage msg) {
                if (msg.getRole() == ChatMessage.MessageRole.USER) {
                        if (msg.getFile() != null) {
                                Media media = new Media(
                                                MimeTypeUtils.parseMimeType(msg.getFile().getFileType()),
                                                new ByteArrayResource(msg.getFile().getData()));
                                return new UserMessage(msg.getContent(), List.of(media));
                        }
                        return new UserMessage(msg.getContent());
                }
                return new AssistantMessage(msg.getContent());
        }

        private void saveAssistantMessage(ChatSession session, String content, String model, int index) {
                ChatMessage assistantMsg = new ChatMessage(session, ChatMessage.MessageRole.ASSISTANT,
                                content, model, null, index);
                chatMessageRepository.save(assistantMsg);
        }

        private String extractContent(ChatResponse response) {
                if (response.getResult() == null || response.getResult().getOutput() == null
                                || response.getResult().getOutput().getContent() == null) {
                        return "";
                }
                return response.getResult().getOutput().getContent();
        }

        private ChatResponseDTO createChatResponse(String chunk, UUID sessionId, String status) {
                return ChatResponseDTO.builder()
                                .chunk(chunk)
                                .sessionId(sessionId)
                                .role(status.equals("data") ? ChatMessage.MessageRole.ASSISTANT : null)
                                .status(status)
                                .build();
        }

        public List<ChatSessionDTO> getSessions() {
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                return chatSessionRepository.findByUserOrderByCreatedAtDesc(user)
                                .stream()
                                .map(ChatSessionDTO::new)
                                .collect(Collectors.toList());
        }

        public List<ChatMessageDTO> getMessagesBySession(UUID sessionId) {
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                ChatSession session = chatSessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Session not found"));
                if (!session.getUser().getId().equals(user.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
                }
                return chatMessageRepository.findBySessionOrderByMessageIndexAsc(session)
                                .stream()
                                .map(ChatMessageDTO::new)
                                .collect(Collectors.toList());
        }

        public List<ModelDTO> getAvailableModels() {
                return AVAILABLE_MODELS;
        }
}
