package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.DbFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizspark.quizsparkserver.config.AiConfig;
import com.quizspark.quizsparkserver.dto.ai.AiResponseDTO;
import com.quizspark.quizsparkserver.dto.ai.ProcessedPageDTO;
import com.quizspark.quizsparkserver.dto.ai.TextDensityResponseDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionDTO;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

@Slf4j
@Service
public class AiFileProcessService {
    private static class ParsingState {
        StringBuilder buffer = new StringBuilder();
        List<String> lastEmitted = new ArrayList<>();
        int balance = 0;
        boolean insideString = false;
        boolean escape = false;
        int startIndex = -1;
        int scanOffset = 0;
    }

    private final QuestionService questionService;
    private final FileService fileService;
    private final PdfProcessorService pdfProcessorService;
    private final ObjectMapper mapper;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public AiFileProcessService(FileService fileService,
                                QuestionService questionService,
                                PdfProcessorService pdfProcessorService,
                                @Qualifier("openAiChatModel") ChatModel chatModel,
                                ObjectMapper mapper,
                                VectorStore vectorStore) {
        this.fileService = fileService;
        this.questionService = questionService;
        this.pdfProcessorService = pdfProcessorService;
        this.chatModel = chatModel;
        this.mapper = mapper;
        this.vectorStore = vectorStore;
    }

    public Flux<AiResponseDTO> extractQuestion(String fileId) {
        DbFile file = fileService.getFileEntityWithContent(fileId);
        String fileType = file.getFileType();

        if (fileType.equals("text/plain") || fileType.equals("application/json") || fileType.equals("text/markdown")) {
            return extractQuestionFromText(file);
        } else if (fileType.equals("application/pdf")) {
            return extractQuestionFromPdf(file);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type " + fileType);
        }
    }

    private Flux<AiResponseDTO> extractQuestionFromText(DbFile file) {
        String fileContent = new String(file.getData());
        String systemMessage = AiConfig.EXTRACT_TEXT_PROMPT + AiConfig.QUESTION_FORMAT_PROMPT;
        String userMessage = "Here is the file content:\n\n" + fileContent;
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemMessage), new UserMessage(userMessage)),
                OpenAiChatOptions.builder().model(AiConfig.TEXT_MODEL).build());
        Flux<AiResponseDTO> start = Flux.just(AiResponseDTO.builder().status("start").build());
        Flux<AiResponseDTO> thinking = Flux.just(AiResponseDTO.builder().status("thinking").build());
        Flux<AiResponseDTO> aiStream = streamAndParseJson(prompt);
        Flux<AiResponseDTO> finish = Flux.just(AiResponseDTO.builder().status("finish").build());

        return Flux.concat(start, thinking, aiStream, finish);
    }

    private Flux<AiResponseDTO> extractQuestionFromPdf(DbFile file) {
        return Flux.just("start")
                .map(status -> AiResponseDTO.builder().status(status).build())
                .flatMap(startNode -> {
                    List<byte[]> images = pdfProcessorService.convertToImages(file);
                    List<Media> mediaList = images.stream()
                            .map(bytes -> new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(bytes)))
                            .toList();
                    Prompt generatedPrompt = new Prompt(
                            List.of(new SystemMessage(AiConfig.EXTRACT_PDF_PROMPT + AiConfig.QUESTION_FORMAT_PROMPT),
                                    new UserMessage("Process these images:", mediaList)),
                            OpenAiChatOptions.builder().model(AiConfig.IMAGE_MODEL).build());

                    Flux<AiResponseDTO> thinking = Flux.just(AiResponseDTO.builder().status("thinking").build());
                    Flux<AiResponseDTO> aiStream = streamAndParseJson(generatedPrompt);
                    Flux<AiResponseDTO> finish = Flux.just(AiResponseDTO.builder().status("finish").build());
                    return Flux.concat(Flux.just(startNode), thinking, aiStream, finish);
                });
    }

    public Flux<AiResponseDTO> generateQuestionFromPracticeHistory(String bankId) throws JsonProcessingException {
        QuestionBankDTO questionBank = questionService.getFullQuestionBankById(bankId);
        List<QuestionDTO> questions = questionBank.getQuestions()
                .stream()
                .peek(question -> {
                    question.setId(null);
                    question.setQuestionBankId(null);
                })
                .collect(Collectors.toList());
        String questionsJson = mapper.writeValueAsString(questions);
        String systemMessage = AiConfig.GENERATE_QUESTION_FROM_HISTORY_PROMPT.formatted(10)
                + "\n\n" + AiConfig.QUESTION_FORMAT_PROMPT;
        String userMessage = "Here is the practice history:\n\n" + questionsJson;
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemMessage), new UserMessage(userMessage)),
                OpenAiChatOptions.builder().model(AiConfig.TEXT_MODEL).build());

        Flux<AiResponseDTO> start = Flux.just(AiResponseDTO.builder().status("start").build());
        Flux<AiResponseDTO> thinking = Flux.just(AiResponseDTO.builder().status("thinking").build());
        Flux<AiResponseDTO> aiStream = streamAndParseJson(prompt);
        Flux<AiResponseDTO> finish = Flux.just(AiResponseDTO.builder().status("finish").build());

        return Flux.concat(start, thinking, aiStream, finish);
    }

    public Flux<AiResponseDTO> extractContent(String fileId) {
        DbFile file = fileService.getFileEntityWithContent(fileId);
        if (file.getFileType().equals("application/pdf")) {
            return extractContentFromPdf(file);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File format is not supported");
    }

    public Flux<AiResponseDTO> extractContentFromPdf(DbFile file) {
        return Flux.<AiResponseDTO>create(sink -> {
            try {
                sink.next(AiResponseDTO.builder().status("start").build());
                // Start stage 1: Generate classification of page type and text density
                List<ProcessedPageDTO> pages = classifyPages(file, sink);
                // Start stage 2: Parse content from pages 
                pages = extractContent(file, pages, sink);
                // Start stage 3: Convert content to a md file and save to the DB.
                pages = saveAsMarkdown(file, pages, sink);
                // Start stage 4: Generate Embeddings and save to file
                generateEmbeddings(file, pages, sink);

                sink.next(AiResponseDTO.builder().status("finish").build());
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private List<ProcessedPageDTO> classifyPages(DbFile file, FluxSink<AiResponseDTO> sink) {
        sink.next(AiResponseDTO.builder().status("next").data("classify").build());
        List<ProcessedPageDTO> pages;
        Optional<DbFile> resultFileOptional = fileService.getDerivedFileWithContent(file, "CLASSIFY RESULTS.json");
        if (resultFileOptional.isPresent()) {
            DbFile resultFile = resultFileOptional.get();
            String content = new String(resultFile.getData());
            try {
                pages = mapper.readValue(content, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Parsing past pages classification results failed");
                pages = pdfProcessorService.splitPages(file);
            }
        } else {
            pages = pdfProcessorService.splitPages(file);
        }

        List<Integer> imageIndices = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            ProcessedPageDTO page = pages.get(i);
            if (!page.isRequireVision() || page.getTextDensity() != null)
                continue;
            imageIndices.add(i);
        }
        if (imageIndices.isEmpty()) {
            try {
                String jsonString = mapper.writeValueAsString(pages);
                sink.next(AiResponseDTO.builder().status("classify").data(jsonString).build());
            } catch (JsonProcessingException e) {
                log.error("Failed to save classification result: {}", e.getMessage());
            }
            return pages;
        }

        List<Media> mediaList = pdfProcessorService.convertToMedias(file, imageIndices);
        UserMessage userMessage = new UserMessage(AiConfig.IMAGE_SORTER_INSTRUCTIONS, mediaList); // Using static final constant
        Prompt prompt = new Prompt(userMessage,
                OpenAiChatOptions.builder().model(AiConfig.IMAGE_MODEL).build());
        ChatResponse response = chatModel.call(prompt);

        if (response != null && response.getResult() != null) {
            Generation generation = response.getResult();
            String content = generation.getOutput().getContent();
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<TextDensityResponseDTO> results = mapper.readValue(content, new TypeReference<>() {});
                for (TextDensityResponseDTO res : results) {
                    if (res.index() >= 0 && res.index() < pages.size()) {
                        int index = imageIndices.get(res.index());
                        ProcessedPageDTO page = pages.get(index);
                        try {
                            page.setTextDensity(ProcessedPageDTO.TextDensity.valueOf(res.density().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            page.setTextDensity(ProcessedPageDTO.TextDensity.MEDIUM); // Fallback
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse AI response: " + content);
            }
        }
        try {
            String jsonString = mapper.writeValueAsString(pages);
            fileService.createOrUpdateDerivedFile(file, "CLASSIFY RESULTS.json", jsonString);
            sink.next(AiResponseDTO.builder().status("classify").data(jsonString).build());
        } catch (JsonProcessingException e) {
            log.error("Failed to save classification result: {}", e.getMessage());
        }
        return pages;
    }

    public List<ProcessedPageDTO> extractContent(DbFile file, List<ProcessedPageDTO> pages, FluxSink<AiResponseDTO> sink) {
        sink.next(AiResponseDTO.builder().status("next").data("content").build());
        Optional<DbFile> resultFileOptional = fileService.getDerivedFileWithContent(file, "EXTRACT RESULTS.json");
        if (resultFileOptional.isPresent()) {
            DbFile resultFile = resultFileOptional.get();
            String content = new String(resultFile.getData());
            try {
                pages = mapper.readValue(content, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Parsing past pages extraction results failed");
            }
        }
        List<Integer> currentBatchPageIndexes = new ArrayList<>();
        double currentBatchCost = 0;
        final double BUCKET_LIMIT = 4000.0;

        for (int i=0; i<pages.size(); i++) {
            if (sink != null) {
                sink.next(AiResponseDTO.builder().status("progress").current(i).total(pages.size()).build());
            }
            ProcessedPageDTO page = pages.get(i);
            if (page.isExtracted()) continue;
            double itemCost = calculateCost(page);
            if (currentBatchCost + itemCost > BUCKET_LIMIT) {
                if (currentBatchCost > 0) {
                    processBatch(file, pages, currentBatchPageIndexes);
                }
                currentBatchPageIndexes.clear();
                currentBatchCost = 0;
            }
            currentBatchCost += itemCost;
            currentBatchPageIndexes.add(i);
        }

        if (currentBatchCost > 0) {
            processBatch(file, pages, currentBatchPageIndexes);
        }
        if (sink != null) {
            try {
                String finalJson = mapper.writeValueAsString(pages);
                sink.next(AiResponseDTO.builder().status("extract").data(finalJson).build());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize final result", e);
            }
        }
        return pages;
    }

    private double calculateCost(ProcessedPageDTO page) {
        if (page.getTextDensity() != null) {
            ProcessedPageDTO.TextDensity density = page.getTextDensity();
            return switch (density) {
                case HIGH -> 1200.0;
                case MEDIUM -> 500.0;
                case LOW -> 150.0;
            };
        } else {
            String text = page.getText();
            int length = (text != null) ? text.length() : 0;
            if (length < 50) {
                return 20.0;
            } else {
                return length * 0.4;
            }
        }
    }

    private void processBatch(DbFile file, List<ProcessedPageDTO> pages, List<Integer> pageIndexes) {
        if (pageIndexes.isEmpty())
            return;
        StringBuilder textBuffer = new StringBuilder();
        int imageIndex = 1;
        for (int pageIndex : pageIndexes) {
            ProcessedPageDTO page = pages.get(pageIndex);
            if (page.isRequireVision())
                textBuffer.append("\n[[IMAGE_").append(imageIndex++).append("]]\n");
            if (page.getText() != null) textBuffer.append(page.getText()).append("\n");
            textBuffer.append("---");
        }
        try {
            List<Integer> mediaIndexes = pageIndexes.stream()
                    .filter(i -> pages.get(i).isRequireVision())
                    .toList();
            List<Media> media = pdfProcessorService.convertToMedias(file, mediaIndexes);
            UserMessage userMessage = new UserMessage(textBuffer.toString(), new ArrayList<>(media));
            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(AiConfig.PDF_TRANSCRIPT_PROMPT), userMessage),
                    OpenAiChatOptions.builder().model(AiConfig.IMAGE_MODEL).build());

            System.out.println("Start Batch Extraction");
            ChatResponse response = chatModel.call(prompt);
            String content = null;
            if (response != null && response.getResult() != null) {
                content = response.getResult().getOutput().getContent();
                System.out.println("Batch Extraction Response: ");
                System.out.println(content);
            }

            if (content != null && !content.isEmpty()) {
                String[] pageParts = content.split("---\n");

                // Parse Bounding Boxes and Crop
                List<List<Integer>> allCrops = new ArrayList<>();
                Pattern cropPattern = Pattern.compile("(?m)^>\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
                
                // First pass: collect all crops from all pages in batch
                for (int i = 0; i < pageIndexes.size(); i++) {
                    if (i < pageParts.length) {
                        Matcher matcher = cropPattern.matcher(pageParts[i]);
                        while (matcher.find()) {
                            allCrops.add(List.of(
                                pageIndexes.get(i),
                                Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)),
                                Integer.parseInt(matcher.group(3)),
                                Integer.parseInt(matcher.group(4))
                            ));
                        }
                    }
                }

                // Process all crops in one go
                List<String> imageUrls = allCrops.isEmpty() ? new ArrayList<>() : pdfProcessorService.cropImages(file, allCrops);
                int urlCounter = 0;

                // Second pass: replace coordinates with URLs
                for (int i = 0; i < pageIndexes.size(); i++) {
                    ProcessedPageDTO page = pages.get(pageIndexes.get(i));
                    if (i < pageParts.length) {
                        String pageText = pageParts[i];
                        Matcher matcher = cropPattern.matcher(pageText);
                        StringBuilder sb = new StringBuilder();
                        while (matcher.find()) {
                            String url = imageUrls.get(urlCounter++);
                            if (url != null) {
                                matcher.appendReplacement(sb, "![Extracted Image](" + url + ")");
                            } else {
                                matcher.appendReplacement(sb, matcher.group(0));
                            }
                        }
                        matcher.appendTail(sb);
                        page.setText(sb.toString().trim());
                        page.setExtracted(true);
                    } else {
                        log.error("[AI ERROR: No Content Generated] - File {}, pageIndex {}", file.getId().toString(), pageIndexes.get(i));
                    }
                }
                try {
                    String jsonString = mapper.writeValueAsString(pages);
                    fileService.createOrUpdateDerivedFile(file, "EXTRACT RESULTS.json", jsonString);
                } catch (JsonProcessingException e) {
                    log.error("Failed to save extraction result: {}", e.getMessage());
                }
            } else {
                log.error("[AI ERROR: No Content Generated] - File {}", file.getId().toString());
            }
        } catch (Exception e) {
            log.error("[AI ERROR: No Content Generated] - File {}", file.getId().toString(), e);
        }
    }

    private List<ProcessedPageDTO> saveAsMarkdown(DbFile file, List<ProcessedPageDTO> pages, FluxSink<AiResponseDTO> sink) {
        StringBuilder content = new StringBuilder();
        for (int i=0; i<pages.size(); i++) {
            ProcessedPageDTO page = pages.get(i);
            content.append(page.getText())
                    .append('\n');
            if (i != pages.size()-1) content.append("---\n");
        }
        fileService.createOrUpdateDerivedFile(file, "CONTENT.md", content.toString());
        sink.next(AiResponseDTO.builder().status("content").data(content.toString()).build());
        return pages;
    }

    private void generateEmbeddings(DbFile file, List<ProcessedPageDTO> pages, FluxSink<AiResponseDTO> sink) {
        sink.next(AiResponseDTO.builder().status("next").data("embed").build());
        List<Document> documents = new ArrayList<>();
        for (ProcessedPageDTO page : pages) {
            if (StringUtils.hasText(page.getText())) {
                Document doc = new Document(page.getText());
                doc.getMetadata().put("fileId", file.getId().toString());
                doc.getMetadata().put("source", file.getFileName());
                documents.add(doc);
            }
        }
        if (!documents.isEmpty()) {
            try {
                vectorStore.add(documents);
            } catch (Exception e) {
                log.warn("Embedding generation failed (Ollama may be down): {}", e.getMessage());
                sink.next(AiResponseDTO.builder().status("error").data("Embedding failed: " + e.getMessage()).build());
            }
        }
    }

    private Flux<AiResponseDTO> streamAndParseJson(Prompt prompt) {
        return chatModel.stream(prompt)
                .map(response -> {
                    if (response.getResult() == null || response.getResult().getOutput() == null) {
                        return "";
                    }
                    String content = response.getResult().getOutput().getContent();
                    return content == null ? "" : content;
                })
                .scan(new ParsingState(), (state, chunk) -> {
                    state.buffer.append(chunk);
                    List<String> newObjects = new java.util.ArrayList<>();

                    int lastEndIndex = -1;

                    for (int i = state.scanOffset; i < state.buffer.length(); i++) {
                        char c = state.buffer.charAt(i);

                        if (state.escape) {
                            state.escape = false;
                            continue;
                        }
                        if (c == '\\') {
                            state.escape = true;
                            continue;
                        }
                        if (c == '"') {
                            state.insideString = !state.insideString;
                            continue;
                        }

                        if (!state.insideString) {
                            if (c == '{') {
                                if (state.balance == 0)
                                    state.startIndex = i;
                                state.balance++;
                            } else if (c == '}') {
                                state.balance--;
                                if (state.balance == 0 && state.startIndex != -1) {
                                    newObjects.add(state.buffer.substring(state.startIndex, i + 1));
                                    lastEndIndex = i;
                                    state.startIndex = -1;
                                }
                            }
                        }
                    }

                    state.scanOffset = state.buffer.length();

                    if (lastEndIndex != -1) {
                        state.buffer.delete(0, lastEndIndex + 1);
                        int removedCount = lastEndIndex + 1;
                        state.scanOffset -= removedCount;
                        if (state.startIndex != -1) {
                            state.startIndex -= removedCount;
                        }
                    }
                    state.lastEmitted = newObjects;
                    return state;
                })
                .flatMapIterable(state -> state.lastEmitted)
                .map(json -> AiResponseDTO.builder().status("data").data(json).build());
    }
}
