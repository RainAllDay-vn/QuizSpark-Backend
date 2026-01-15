package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.config.AiConfig;
import com.quizspark.quizsparkserver.dto.chat.*;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.models.ChatItem.ChatItemType;
import com.quizspark.quizsparkserver.repositories.ChatMessageRepository;
import com.quizspark.quizsparkserver.repositories.ChatSessionRepository;
import com.quizspark.quizsparkserver.repositories.FileRepository;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
    private final FileRepository fileRepository;
    private final UserService userService;
    private final ChatModel chatModel;

    @Transactional
    public Flux<ChatResponseDTO> streamChat(ChatRequestDTO request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = userService.getUserById(user.getId());
        ChatSession session = getOrCreateSession(request, user);
        ChatMessage userMessage = convertToChatMessage(request, session);
        handleMessageTruncation(session, request.getIndex());
        chatMessageRepository.save(userMessage);
        return executeChatStream(session, request.getModel(), userMessage.getMessageIndex() + 1);
    }

    /**
     * Converts a ChatRequestDTO to a ChatMessage object.
     * This method creates a USER role message and processes all items including attachments.
     *
     * @param request The ChatRequestDTO containing the message data
     * @return A ChatMessage object
     */
    public ChatMessage convertToChatMessage(ChatRequestDTO request, ChatSession session) {
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSession(session);
        int nextIndex = chatMessageRepository.countBySession(session).intValue();
        userMessage.setMessageIndex(nextIndex);
        userMessage.setRole(ChatMessage.MessageRole.USER);
        userMessage.setModel(request.getModel());

        List<ChatItemDTO> chatItemDTOS = request.getItems();
        for (int i=0; i<chatItemDTOS.size(); i++) {
            ChatItemDTO itemDTO = chatItemDTOS.get(i);

            if (itemDTO.getFileId() != null) {
                // Use existing file from DB
                DbFile dbFile = fileService.getFileEntityWithContent(itemDTO.getFileId().toString());
                ChatItem item = itemDTO.toChatItem(i, dbFile);
                userMessage.addItem(item);
            } else if (itemDTO.getType() == ChatItemDTO.ChatItemType.MESSAGE) {
                // Create new file from content (Base64)
                ChatItem item = itemDTO.toChatItem(i, itemDTO.getContent());
                userMessage.addItem(item);
            } else {
                byte[] data = Base64.getDecoder().decode(itemDTO.getContent());
                DbFile newFile = new DbFile(itemDTO.getFileName(), itemDTO.getFileType(), data, null);
                ChatItem item = itemDTO.toChatItem(i, newFile);
                userMessage.addItem(item);
            }
        }
        return userMessage;
    }

    private ChatSession getOrCreateSession(ChatRequestDTO request, User user) {
        if (request.getSessionId() == null) {
            String title = "New Chat";
            for (ChatItemDTO item : request.getItems()) {
                if (item.getType() == ChatItemDTO.ChatItemType.MESSAGE && item.getContent() != null) {
                    title = item.getContent().substring(0, Math.min(item.getContent().length(), 50));
                    break;
                }
            }
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
            List<ChatMessage> messagesToDelete = chatMessageRepository
                    .findBySessionAndMessageIndexGreaterThanEqual(session, index);
            if (!messagesToDelete.isEmpty()) {
                List<UUID> fileIdsToCheck = messagesToDelete.stream()
                        .flatMap(m -> m.getFiles().stream())
                        .map(DbFile::getId)
                        .distinct()
                        .collect(Collectors.toList());

                chatMessageRepository.deleteAll(messagesToDelete);

                if (!fileIdsToCheck.isEmpty()) {
                    fileRepository.deleteFilesWithNullUserByIds(fileIdsToCheck);
                }
            }
        }
    }

    private Flux<ChatResponseDTO> executeChatStream(ChatSession session, String modelName, int assistantIndex) {
        String model = modelName != null ? modelName : AiConfig.CHAT_DEFAULT_MODEL;
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
                    if (!fullResponse.isEmpty()) {
                        saveAssistantMessage(session, fullResponse.toString(), model,
                                assistantIndex);
                        logChatRequest(prompt, fullResponse.toString());
                    }
                })
                .startWith(Flux.just(createChatResponse("", sessionId, "start")))
                .concatWith(Flux.just(createChatResponse("", sessionId, "finish")))
                .onErrorResume(e -> {
                    logChatRequest(prompt, fullResponse.toString());
                    return Flux.just(createChatResponse(
                            "Error: The AI provider is currently unavailable. Please try again later.",
                            sessionId, "error"));
                });
    }

    private Prompt createPrompt(ChatSession session, String model) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AiConfig.CHAT_SYSTEM_PROMPT));

        List<ChatMessage> chatMessages = chatMessageRepository.findBySessionOrderByMessageIndexAsc(session);
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage msg = chatMessages.get(i);
            boolean isLatest = (i == chatMessages.size() - 1);
            messages.add(convertToAiMessage(msg, isLatest));
        }

        return new Prompt(messages, OpenAiChatOptions.builder().model(model).build());
    }

    private Message convertToAiMessage(ChatMessage chatMessage, boolean includeFiles) {
        StringBuilder textContent = new StringBuilder();
        List<Media> medias = new ArrayList<>();

        for (ChatItem item : chatMessage.getItems()) {
            if (item.getType() == ChatItemType.MESSAGE) {
                if (item.getContent() != null) {
                    textContent.append(item.getContent()).append("\n");
                    continue;
                }
            }
            if (!includeFiles) {
                continue;
            }
            if (item.getType() == ChatItemType.ATTACHMENT) {
                if (item.getFile()==null) {
                    textContent.append("\n[Text Attachment]")
                            .append(item.getContent())
                            .append("\n");
                    continue;
                } else if (isTextFile(item.getFile())) {
                    textContent.append("\n[Text Attachment]")
                            .append(new String(item.getFile().getData(), StandardCharsets.UTF_8))
                            .append("\n");
                    continue;
                } else if (isImageFile(item.getFile())) {
                    textContent.append("\n[Image Attachment - Reference #")
                            .append(medias.size()+1)
                            .append("]\n");
                    medias.add(new Media(
                            MimeTypeUtils.parseMimeType(item.getFile().getFileType()),
                            new ByteArrayResource(item.getFile().getData())));
                    continue;
                }
                else continue;
            }
            if (item.getType() == ChatItemType.CONTEXT) {
                processContext(textContent, medias, item);
            }
        }

        if (chatMessage.getRole() == ChatMessage.MessageRole.USER) {
            if (!medias.isEmpty()) {
                return new UserMessage(textContent.toString().trim(), medias);
            }
            return new UserMessage(textContent.toString().trim());
        }
        return new AssistantMessage(textContent.toString().trim());
    }

    private boolean isTextFile(DbFile dbFile) {
        if (dbFile.getFileType().startsWith("text")) return true;
        if (dbFile.getFileType().equals("application/json")) return true;
        return false;
    }

    private boolean isImageFile(DbFile dbFile) {
        return dbFile.getFileType().startsWith("image");
    }

    private void processContext(StringBuilder textContent, List<Media> medias, ChatItem item) {
        if (item.getDescription().equals("WORKSPACE FILE")) {
            if (item.getFile()==null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workspace File context should contain file or file id");
            } 
            DbFile file = item.getFile();
            if (isTextFile(file)) {
                textContent.append("[WORKSPACE FILE - ").append(file.getFileName()).append("]\n")
                        .append("The following is the text content of the file the user is currently viewing in their workspace:\n")
                        .append("---\n")
                        .append(new String(item.getFile().getData(), StandardCharsets.UTF_8))
                        .append("\n---\n\n");
            } else if (isImageFile(item.getFile())) {
                textContent.append("\n[WORKSPACE FILE (Image) - ").append(file.getFileName())
                        .append(" - Reference #")
                        .append(medias.size()+1)
                        .append("]\n")
                        .append("The user is currently viewing this image in their workspace.\n\n");
                medias.add(new Media(
                        MimeTypeUtils.parseMimeType(item.getFile().getFileType()),
                        new ByteArrayResource(item.getFile().getData())));
            }
        }
    }

    private String extractContent(ChatResponse response) {
        if (response.getResult() == null
                || response.getResult().getOutput() == null
                || response.getResult().getOutput().getContent() == null) {
            return "";
        }
        return response.getResult().getOutput().getContent();
    }

    private void saveAssistantMessage(ChatSession session, String content, String model, int index) {
        ChatMessage assistantMsg = new ChatMessage(session, ChatMessage.MessageRole.ASSISTANT,
                model, index);
        ChatItem item = new ChatItem(assistantMsg, ChatItemType.MESSAGE, content, null, 0);
        assistantMsg.addItem(item);
        chatMessageRepository.save(assistantMsg);
    }

    private void logChatRequest(Prompt prompt, String aiResponse) {
        String userMessage = "N/A";
        int mediaCount = 0;

        List<Message> messages = prompt.getInstructions();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg instanceof UserMessage userMsg) {
                userMessage = userMsg.getContent();
                mediaCount = userMsg.getMedia() != null ? userMsg.getMedia().size() : 0;
                break;
            }
        }

        log.info("\n--- Chat Request Log ---\nUser Message: {}\nMedia Count: {}\nAI Response: {}\n------------------------",
                userMessage, mediaCount, aiResponse);
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
        return getSessions(user);
    }

    public List<ChatSessionDTO> getSessions(User user) {
        return chatSessionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ChatSessionDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session not found"));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        List<ChatMessage> messages = chatMessageRepository.findBySessionOrderByMessageIndexAsc(session);
        List<UUID> fileIdsToCheck = messages.stream()
                .flatMap(m -> m.getFiles().stream())
                .map(DbFile::getId)
                .distinct()
                .collect(Collectors.toList());

        chatMessageRepository.deleteAll(messages);
        chatSessionRepository.delete(session);

        if (!fileIdsToCheck.isEmpty()) {
            fileRepository.deleteFilesWithNullUserByIds(fileIdsToCheck);
        }
    }

    public List<ChatMessageDTO> getMessagesBySession(UUID sessionId) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserById(principal.getId());
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
        return AiConfig.AVAILABLE_CHAT_MODELS;
    }
}
