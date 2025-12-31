package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.AiResponseDTO;
import com.quizspark.quizsparkserver.models.DbFile;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class AiService {
    private final FileService fileService;
    private final PdfProcessorService pdfProcessorService;
    private final ChatModel chatModel;
    private static final String TEXT_MODEL = "deepseek-ai/DeepSeek-V3.2-TEE";
    private static final String IMAGE_MODEL = "zai-org/GLM-4.6V";
    private static final String PARSE_TEXT_PROMPT = """
            ### Primary Objective

            You are an AI agent specialized in converting text-based documents (such as `.txt`, `.md`, or raw `.json` content) containing educational content, quizzes, or question sets into a structured JSON format that matches the `QuestionCreationDTO` structure. Your task is to extract questions from the text input and format them according to the specified schema.

            ### Input

            You will receive raw text containing questions, which may include:

            * Multiple choice questions
            * Multiple answer questions
            * Fill-in-the-blank questions
            * Open-ended questions
            * Mixed content with explanations
            * **Rich text elements** represented in text (e.g., Markdown tables, code blocks, bold/italic markers).
            * **Context-dependent questions** (e.g., "Questions 1-3 refer to the following passage...").

            **Note:** The input contains only text. All visual clues (images, diagrams) have been removed.

            ### Output Format

            You must output a JSON array of question objects, where each object follows the `QuestionCreationDTO` structure:

            ```json
            [
              {
                "questionType": "SINGLE_ANSWER|MULTIPLE_ANSWER|FILL_THE_BLANK|OPEN_ANSWER",
                "tags": ["AI_GENERATED", "tag2"],
                "description": "The complete question text with Markdown",
                "answer": ["index_or_text"],
                "choices": ["choice1", "choice2", "..."],
                "explanation": "Optional explanation text"
              }
            ]

            ```

            ### Field Definitions and Requirements

            #### questionType (Required)

            * **SINGLE_ANSWER**: Questions with exactly one correct answer from multiple choices
            * **MULTIPLE_ANSWER**: Questions with multiple correct answers from multiple choices
            * **FILL_THE_BLANK**: Questions with blanks to be filled, where choices represent possible answers
            * **OPEN_ANSWER**: Open-ended questions without predefined choices

            #### tags (Required)

            * Array of strings categorizing the question.
            * **MANDATORY RULE**: Every single question **must** include the tag **"AI_GENERATED"**.
            * **"NO ANSWER"**: Must be added if the correct answer is not indicated in the text.
            * **RESTRICTION**: Do **NOT** include subject or topic tags (e.g., "math", "history"). Do **NOT** use "GRAPH" tags.

            #### description (Required)

            * The complete question text.
            * **Context Propagation**: If a question is part of a set that shares a common prompt (e.g., "Read the following passage and answer questions 1-3"), you **must include the shared text/passage/table** in the description of **every** question it applies to.
            * **Markdown Formatting**: Use Markdown to preserve formatting found in the text.
            * **Bold/Italics**: Keep emphasis (e.g., `**bold**`, `*italic*`).
            * **Tables**: Convert text-based tables into Markdown table syntax.
            * **Code**: Use code blocks (```) for programming questions.



            #### answer (Conditional)

            * **For SINGLE_ANSWER**: Array with exactly one element containing the index (as string) of the correct choice (e.g., `["0"]` for first choice).
            * **For MULTIPLE_ANSWER**: Array with one or more elements containing indices (as strings) of correct choices (e.g., `["0", "2"]`).
            * **For FILL_THE_BLANK**: Array containing the actual text answers for each blank (not indices).
            * **For OPEN_ANSWER**: Should be null or omitted.
            * **IMPORTANT**: If no answer is found in the file, use default choice `["0"]` and add "NO ANSWER" to the tags.

            #### choices (Conditional)

            * **For SINGLE_ANSWER**: Array with at least 2 choices.
            * **For MULTIPLE_ANSWER**: Array with at least 2 choices.
            * **For FILL_THE_BLANK**: Should be null or empty array (choices are not used for fill-in-the-blank).
            * **For OPEN_ANSWER**: Should be null or omitted.
            * **Formatting**: Choices should support Markdown.

            #### explanation (Optional)

            * Text explaining the answer or providing additional context.
            * **STRICT RULE**: Only include this if the explanation is **explicitly provided** in the source text (e.g., in an answer key section).
            * Do **not** generate or infer your own explanations.

            ### Processing Guidelines

            #### 1. Context Propagation (CRITICAL)

            Many text files group questions under a single header, passage, or data table.

            * **Detect**: Look for indicators like "Questions 10–12 refer to the following passage," or a table/scenario placed before a series of questions.
            * **Action**: You must **prepend** that shared context to the `description` field of **every** individual question in that group.
            * **Reasoning**: The system treats every question as an independent entity. If you do not copy the context, the individual question will make no sense to the user.

            #### 2. Markdown & Rich Text Formatting

            The input text may contain formatting symbols.

            * **Tables**: If the question relies on a text table, render it using Markdown syntax:
            ```markdown
            | Column 1 | Column 2 |
            | -------- | -------- |
            | Data A   | Data B   |

            ```


            * **Equations**: Use LaTeX syntax wrapped in single `$` for inline math.
            * **Line Breaks**: Ensure distinct paragraphs are separated by newlines (`\n\n`) in the string.

            #### 3. Answer Extraction Rules

            Prioritize finding the correct answer using these text-based cues:

            1. **Explicit Marking**: Asterisks (*), checkmarks (v or ✓), arrows (->) next to a choice.
            2. **Text Formatting**: Markdown-style bold/italic markers on specific options (e.g., `*Option A*`) that differ from incorrect ones.
            3. **Answer Keys**: Look at the end of the text stream for a key.

            #### 4. Missing Answers ("NO ANSWER" Tag)

            If—and only if—**none** of the extraction rules yield a correct answer:

            1. Use the first choice as the default answer: `["0"]`
            2. Add **"NO ANSWER"** to the tags array (alongside "AI_GENERATED").
            3. Do **not** attempt to solve the question yourself.

            ### Examples

            #### Example 1: Shared Context & Markdown Table

            **Input Text:**

            > Questions 1-2 refer to the following table:
            > Student | Grade
            > --- | ---
            > Alex | 85
            > Sam | 92
            > 1. Who got the highest grade?
            > A) Alex
            > B) Sam *
            >
            >

            **Expected Output:**

            ```json
            {
              "questionType": "SINGLE_ANSWER",
              "tags": ["AI_GENERATED"],
              "description": "**Questions 1-2 refer to the following table:**\n\n| Student | Grade |\n| --- | --- |\n| Alex | 85 |\n| Sam | 92 |\n\nWho got the highest grade?",
              "answer": ["1"],
              "choices": ["Alex", "Sam"],
              "explanation": null
            }

            ```

            #### Example 2: No Answer Key Present

            **Input Text:**

            > 5. What is the capital of France?
            > A. Berlin
            > B. Madrid
            > C. Paris
            >
            >

            **Expected Output:**

            ```json
            {
              "questionType": "SINGLE_ANSWER",
              "tags": ["AI_GENERATED", "NO ANSWER"],
              "description": "What is the capital of France?",
              "answer": ["0"],
              "choices": ["Berlin", "Madrid", "Paris"],
              "explanation": null
            }

            ```

            ### Quality Assurance

            Before finalizing your output, verify:

            1. **Tag Check**: Does **every** question contain `"AI_GENERATED"` in the `tags` array?
            2. **Context Check**: If the question refers to a passage/table, is that content included in the `description`?
            3. **Visual Check**: Ensure no "GRAPH" tags are used.
            4. **Format Check**: Are tables and bold text correctly formatted in Markdown?
            5. **Answer Check**: If the answer is missing, is "NO ANSWER" in the tags?
            6. **Validity**: JSON syntax is valid.
            """;

    public AiService(FileService fileService, PdfProcessorService pdfProcessorService, ChatModel chatModel) {
        this.fileService = fileService;
        this.pdfProcessorService = pdfProcessorService;
        this.chatModel = chatModel;
    }

    public Flux<AiResponseDTO> parse(String fileId) {
        DbFile file = fileService.getFileEntity(fileId);
        String fileType = file.getFileType();

        if (fileType.equals("text/plain") || fileType.equals("application/json") || fileType.equals("text/markdown")) {
            return parseText(file);
        } else if (fileType.equals("application/pdf")) {
            pdfProcessorService.processPdf(fileId);
            return Flux.just(AiResponseDTO.builder().status("finish").build());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type " + fileType);
        }
    }

    private Flux<AiResponseDTO> parseText(DbFile file) {
        String fileContent = new String(file.getData());
        String userMessage = "Here is the file content:\n\n" + fileContent;
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(PARSE_TEXT_PROMPT), new UserMessage(userMessage)),
                OpenAiChatOptions.builder().model(TEXT_MODEL).build());

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
                .map(json -> AiResponseDTO.builder().status("data").data(json).build())
                .startWith(AiResponseDTO.builder().status("start").build())
                .concatWith(Flux.just(AiResponseDTO.builder().status("finish").build()));
    }

    public record ImageAnalysisDTO(int index, String type, String density) {
    }

    public Flux<ImageAnalysisDTO> sortImages(List<String> fileIds) {
        if (fileIds.size() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch size must not exceed 50 images");
        }

        List<Media> mediaList = new java.util.ArrayList<>();
        for (String fileId : fileIds) {
            DbFile file = fileService.getFileEntity(fileId);
            // Assuming PNG for now, or detect from fileType
            // The file.getData() returns byte[]
            // MimeType is needed
            org.springframework.util.MimeType mimeType = MimeTypeUtils.parseMimeType(file.getFileType());
            mediaList.add(new Media(mimeType, new ByteArrayResource(file.getData())));
        }

        String instructions = """
                Analyze these images. For each, identify the document type and classify text density as 'High' (dense text), 'Medium' (forms/letters), or 'Low' (receipts/cards).
                Return ONLY a JSON list: [{"index": 0, "type": "contract", "density": "High"}, ...].
                Use the index 0, 1, 2... corresponding to the order of images provided.
                """;

        UserMessage userMessage = new UserMessage(instructions, mediaList);

        Prompt prompt = new Prompt(userMessage,
                OpenAiChatOptions.builder().model(IMAGE_MODEL).build());

        return chatModel.stream(prompt)
                .map(response -> {
                    if (response.getResult() == null || response.getResult().getOutput() == null) {
                        return "";
                    }
                    String content = response.getResult().getOutput().getContent();
                    return content == null ? "" : content;
                })
                .scan(new ParsingState(), (state, chunk) -> {
                    // Reuse the same parsing logic as parseText since it parses a JSON list
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
                .map(jsonString -> {
                    // Manual simple parsing or use Jackson
                    // Since specific dependencies aren't visible, I'll use a simple regex or assume
                    // Jackson is available but not injected here.
                    // Given the project uses Spring, Jackson is likely available.
                    // But to avoid adding ObjectMapper dependency just for this method if not
                    // already present (it's not fields),
                    // I will do a quick manual parse or new ObjectMapper.
                    // Actually, I can use a simpler approach or just try to use ObjectMapper.
                    // Let's create a local ObjectMapper for now.
                    try {
                        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(jsonString,
                                ImageAnalysisDTO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse JSON: " + jsonString, e);
                    }
                });
    }

    private static class ParsingState {
        StringBuilder buffer = new StringBuilder();
        List<String> lastEmitted = new java.util.ArrayList<>();
        int balance = 0;
        boolean insideString = false;
        boolean escape = false;
        int startIndex = -1;
        int scanOffset = 0;
    }
}
