package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.DbFile;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizspark.quizsparkserver.dto.ai.AiResponseDTO;
import com.quizspark.quizsparkserver.dto.ai.ProcessedPageDTO;
import com.quizspark.quizsparkserver.dto.ai.TextDensityResponseDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionDTO;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

@Service
public class AiService {
    private static class ParsingState {
        StringBuilder buffer = new StringBuilder();
        List<String> lastEmitted = new java.util.ArrayList<>();
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
    private static final String TEXT_MODEL = "deepseek-ai/DeepSeek-V3.2-TEE";
    private static final String IMAGE_MODEL = "Qwen/Qwen3-VL-235B-A22B-Instruct";
    private static final String EXTRACT_TEXT_PROMPT = """
            ### Content Analysis & Extraction Protocol

            **Objective**
            Analyze the provided raw text to identify educational questions, context, and answers. The input text may be derived from `.txt`, `.md`, or parsed `.json` files. You must normalize this content into a coherent structure.

            **Input Characteristics**
            * **Raw Text:** The input contains only text; visual diagrams have been removed.
            * **Rich Text:** Expect Markdown tables, code blocks, or bold/italic markers representing emphasis.
            * **Grouping:** Questions may be grouped by a common passage or scenario.

            **Processing Rules**

            #### 1. Context Propagation (CRITICAL)
            * **Detection:** detailed scanning for phrases like "Questions 1-3 refer to the following passage," or data tables placed immediately before a series of numbered questions.
            * **Association:** You must associate that shared context (passage/table) with **every** individual question it applies to.
            * **Reasoning:** Extracted questions will be used independently; without the propagated context, the question is unsolvable.

            #### 2. Rich Text Normalization
            * **Tables:** Detect text-based table structures and convert them into standard Markdown table syntax.
            * **Equations:** specific mathematical expressions must be wrapped in LaTeX syntax (e.g., `^2$`).
            * **Code:** Detect programming segments and wrap them in code blocks (```).
            * **Formatting:** Preserve bold/italics found in the source text.

            #### 3. Answer Identification Logic
            Analyze the text to determine the correct answer using the following hierarchy of cues:
            1.  **Explicit Markers:** Look for asterisks (*), checkmarks (v or ✓), or arrows (->) placed immediately next to a specific choice.
            2.  **Formatting Cues:** Look for choices that are bolded or italicized differently from the distractors (e.g., `*Option A*`).
            3.  **Answer Keys:** Scan the end of the document/text stream for a separated answer key section.

            **Missing Answer Handling:**
            * If **none** of the above cues yield a confirmed answer, mark the question internally as "Unsolved/Missing Answer."
            * Do **not** attempt to solve the question using your own knowledge.
            """;
    private static final String EXTRACT_PDF_PROMPT = """
            ### Document Processing Protocol: PDF/Image Vision Analysis

            **Objective**
            You are a Vision-Language AI. You will be provided with a series of images representing pages from an educational PDF document. Your task is to visually scan these pages, perform OCR (Optical Character Recognition), and extract structured questions, context, and answers into a normalized format.

            **Input Characteristics**

            * **Visual Input:** You are viewing raw page images. Expect multi-column layouts, headers/footers, and varied font sizes.
            * **Visual Artifacts:** Expect handwriting (circles around answers), watermarks, or scan noise.
            * **Content Types:** Educational quizzes, reading passages, worksheets, and answer keys.

            **Processing Rules**

            #### 1. Visual Layout & Reading Order

            * **Column Detection:** Analyze the page layout before reading. If the text is arranged in columns, read top-to-bottom, left-to-right. Do not read across the page line-by-line if it breaks the column structure.
            * **Noise Filtration:** Ignore non-content elements such as:
            * Page numbers (e.g., "Page 1 of 5").
            * Running headers/footers.
            * Watermarks that overlap text.



            #### 2. Context Propagation (Visual Grouping)

            * **Visual Grouping:** Identify questions that are physically grouped under a shared text block, image, or table.
            * **Indicators:** Look for visual cues like a box surrounding a text passage, or a large bold header stating "Read the text below and answer questions 5-10."
            * **Action:** You must transcribe this shared context and associate it with **every** individual question in that visual group.

            #### 3. Rich Text & Visual Transcription

            * **Tables:** Visually identify grid lines or spatial alignment that indicates a table. Transcribe this into standard Markdown table syntax.
            * **Math/Equations:** Convert visual mathematical notation (fractions, integrals, superscripts) into LaTeX format (e.g., `$\frac{1}{2}$`).
            * **Images:** If a question relies on a diagram or image (e.g., "Look at the diagram below"), insert a placeholder description in brackets, e.g., `[Diagram showing a cell structure]`. Do not skip the question.

            #### 4. Answer Identification (Visual Cues)

            Prioritize finding the correct answer using visual markers often found in scanned PDFs:

            1. **Handwritten Marks:** Look for hand-drawn circles, checkmarks, or underlines indicating a selected choice.
            2. **Visual Highlights:** Look for text with a colored background or distinct bolding compared to other options.
            3. **Answer Keys:** Check the final images in the sequence for a dedicated "Answer Key" section. Map the numbers in the key back to the questions found on previous pages.

            **Missing Answer Handling:**

            * If **none** of the extraction rules yield a correct answer, mark the question internally as "Unsolved/Missing Answer."
            * Do **not** attempt to solve the question using your own knowledge.
            """;
    private static final String GENERATE_QUESTION_FROM_HISTORY_PROMPT = """
            # Role
            You are an Intelligent Adaptive Learning Engine. Your goal is to analyze a user's historical performance data and dynamically generate a new curriculum of %d questions to optimize their learning path.

            # Input Data Explanation
            You will receive a list of objects representing the user's history. These objects follow a specific schema called "QuestionDTO". Here is how to interpret the fields:
            - `tags` (List<String>): The specific topics or concepts covered by the question.
            - `description` (String): The actual question text the user answered.
            - `answer` (List<String>): The correct solution.
            - `choices` (List<String>): The options provided to the user (including the correct answer).
            - `totalAttempts` (Integer): The number of times the user has tried this specific question.
            - `correctAttempts` (Integer): The number of times the user answered correctly.

            # Analytical Process
            Before generating new content, you must analyze the "QuestionDTO" list to determine the user's proficiency level for each tag.
            1. **Calculate Mastery:** For each tag, calculate the success rate (`correctAttempts` / `totalAttempts`).
            2. **Determine Needs:**
               - **Low Mastery (< 60%):** The user struggles here. These areas require **Reinforcement**.
               - **High Mastery (> 85%):** The user is proficient. These areas require **Challenge/Deepening**.
               - **Moderate Mastery (60-85%):** The user is inconsistent. These areas require **Practice**.

            # Generation Strategy
            Generate exactly %d new questions. You must determine the best balance of questions based on the analysis above:

            1. **If the user has many Low Mastery tags:**
               - Prioritize **Reinforcement**.
               - Create variations of the failed concepts but simplify the context.
               - Focus on "What" and "Why" questions (Lower Bloom's Taxonomy) to build a solid foundation.
               - Ensure distractors (wrong choices) specifically target the misconceptions implied by their previous failures.

            2. **If the user has mostly High Mastery tags:**
               - Prioritize **Challenge**.
               - Create questions that combine multiple tags or introduce edge cases.
               - Focus on "How", "Analyze", and "Evaluate" questions (Higher Bloom's Taxonomy).
               - If possible, introduce adjacent topics (topics related to the tags but not explicitly present) to widen their knowledge.

            3. **If the user is balanced or inconsistent:**
               - Provide a mix of reinforcement and slightly harder variations to stabilize their knowledge.

            # Output Requirements
            - Generate exactly %d questions.
            - Maintain the subject matter context provided in the input `tags`.
            - Ensure all generated questions are factually correct.
            """;
    private static final String IMAGE_SORTER_INSTRUCTIONS = """
            Role: Academic Document Density Classifier
            Task: Analyze images to determine transcription token cost.

            CONTEXT: The user is processing academic materials (slides, exams, notebooks, papers).

            Classification Criteria:

            1. HIGH (Dense Text | >800 tokens):

            * Typed academic papers (single-spaced, 2-column).
            * Textbook pages or dense reading comprehension passages.
            * Reference lists or bibliographies.
            * (Visual: Small printed font, minimal margins, wall of text).

            2. MEDIUM (Standard Text | 300-800 tokens):

            * Handwritten Notebooks (Full page of writing).
            * Scanned Exam Papers (Questions mixed with answer space).
            * Syllabus documents or detailed handouts.
            * Dense Lecture Slides (Slides containing full paragraphs of text).
            * (Visual: Large handwriting, or typed text with significant spacing/formatting).

            3. LOW (Sparse Text | <300 tokens):

            * Standard Lecture Slides (Bullet points, headers, diagrams).
            * Whiteboard photos.
            * Flashcards or sticky notes.
            * Sparse handwriting or math equations.
            * (Visual: Mostly background, images, or large font sizes).

            Input: A sequence of images.
            Output: A strict JSON list of objects representing the input order.

            Constraints:

            * Do NOT transcribe text.
            * Treat "Handwriting" as MEDIUM or LOW, never HIGH (handwriting has low token density).
            * Output ONLY the raw JSON string.
            * Do NOT include markdown formatting, backticks (e.g., ```json), or any introductory/concluding text.

            Format: [{"index": 0, "density": "HIGH"}, {"index": 1, "density": "LOW"}]
            """;

    private static final String PDF_TRANSCRIPT_PROMPT = """
            ### Input Parsing Logic (CRITICAL)
            You will receive a text stream containing indexed placeholders like `[[IMAGE_1]]`, `[[IMAGE_2]]`.
            - **Mapping:** `[[IMAGE_N]]` corresponds strictly to the **Nth image attachment** provided in this request.
            - **Execution:** When you encounter `[[IMAGE_N]]` in the text, immediately stop reading the text, process the corresponding image using the "Image Input Handling" rules below, insert the result, and then resume reading the text.

            ### Processing Logic
            Process the input stream sequentially. Do not summarize. Do not skip content.

            #### 1. Page Delimiting (STRICT REQUIREMENT)
            - **Mandatory Split:** You **MUST** append the exact token `<|PAGE_SPLIT_TOKEN|>` at the very end of the content for every single input page processed (whether it was a Text chunk or an Image).
            - **Empty Pages:** If an input page/image is blank, output **nothing but** `<|PAGE_SPLIT_TOKEN|>`.

            #### 2. Visual Annotation Handling (Handwriting & Markups)
            Interpret human markings (red pen, highlighters, pencil) on images using these rules:
            - **Circled Text (Focus):** Treat as High Importance. Format as **Bold** or place in a Blockquote (`>`).
              - *Example:* `> **The core formula is...**`
            - **Underline (Emphasis):** Format as *Italics* or **Bold**.
              - *Example:* `The *velocity* is constant.`
            - **Cross-out / Strike (Correction):** Use Strikethrough (`~~`) and follow with the correction if present.
              - *Example:* `~~The initial value~~ The final value`
            - **Arrow + Note (Marginalia):** Insert inline as `[Note: ...]` near the referenced text.
              - *Example:* `Energy [Note: Check units!] is conserved.`
            - **Checkmark (✓) (Verification):** Append `[✓]` to the verified item.
              - *Example:* `Answer: 42 [✓]`

            #### 3. Text Input Handling
            - **Formatting:** Clean up raw text by fixing spacing, indentation, and broken line breaks caused by PDF extraction.
            - **Structure:** Apply semantic Markdown (e.g., convert "Chapter 1" to `## Chapter 1`, bold key terms).
            - **Continuity:** If text immediately before `[[IMAGE_N]]` ends mid-sentence, hold the context and merge it seamlessly with the transcription of `[[IMAGE_N]]` without inserting a new line.

            #### 4. Image Input Handling
            Analyze the visual content of the referenced image to determine the processing mode:

            * **Mode A: Text-Heavy (Slides, Pages, Exam Questions)**
                * **Action:** Perform **Verbatim Transcription**, applying the "Annotation Handling" rules above.
                * **Math:** Convert all equations to LaTeX format (surrounded by `$$` for display or `$` for inline).
                * **Tables:** Reconstruct tables using Markdown table syntax.

            * **Mode B: Visual-Heavy (Charts, Diagrams)**
                * **Action:** Provide a structured description.
                * **Format:** `> **[Visual Description]:** A [chart/diagram] showing... [Capture all axis labels, data points, and key insights].`

            * **Mode C: Hybrid (Text wrapping an image)**
                * **Action:** Transcribe the text verbatim and insert the visual description in its logical position within the flow.

            ### Formatting Rules
            1.  **Unified Style:** Ensure font styles in the OCR'd text match the raw text style.
            2.  **No Filler:** Output **strictly** the Markdown content. Do not output "Here is the text", "Sure", or code block fences (```markdown).

            ### Edge Case Handling
            - **Illegible Text:** Mark as `[?]`.
            - **Split Sentences:** If Text Input ends with "The coefficient of" and the subsequent Image starts with "friction is...", output "The coefficient of friction is..." on one line.
            """;
    private static final String QUESTION_FORMAT_PROMPT = """
            This prompt focuses strictly on the **JSON structure, data types, and validation constraints**. It is designed to be appended to the processing prompt or used as a function definition.

            ```markdown
            ### JSON Output Schema & Formatting Rules

            **Task**
            Map the extracted content into a JSON array of `QuestionCreationDTO` objects.

            **Target Schema**
            ```json
            [
              {
                "questionType": "ENUM_VALUE",
                "tags": ["AI_GENERATED", "TAG_2"],
                "description": "Full question text with context",
                "answer": ["index_string" or "text_value"],
                "choices": ["choice_1", "choice_2"],
                "explanation": "Extracted explanation or null"
              }
            ]

            ```

            **Field Definitions & Constraints**

            #### 1. questionType (Enum)

            * `SINGLE_ANSWER`: One correct option. `answer` array contains one index string.
            * `MULTIPLE_ANSWER`: Multiple correct options. `answer` array contains multiple index strings.
            * `FILL_THE_BLANK`: User inputs text. `choices` must be null/empty. `answer` contains the text strings.
            * `OPEN_ANSWER`: Free text response. `answer` and `choices` must be null.

            #### 2. tags (Array<String>)

            * **Mandatory:** Must always include `"AI_GENERATED"`.
            * **Conditional:** Must include `"NO ANSWER"` if the answer could not be identified in the source text.
            * **Prohibited:** Do NOT include subject tags (e.g., "Math", "History") or "GRAPH" tags.

            #### 3. description (String)

            * Contains the full question stem.
            * **Context Inclusion:** Must start with any shared passages/tables identified during processing.
            * **Format:** Must be valid Markdown string (e.g., `**bold**`, `| Table |`).

            #### 4. answer (Array<String>)

            * **Index-Based:** For Single/Multiple choice, use string indices (e.g., `["0"]` for A, `["1"]` for B).
            * **Text-Based:** For Fill-the-blank, use the exact text string.
            * **Defaulting Rule:** If tags include `"NO ANSWER"`, set this to `["0"]`.

            #### 5. choices (Array<String>)

            * Required for Single/Multiple choice (min 2 items).
            * Must be `null` or empty for Fill-the-blank/Open-ended.

            #### 6. explanation (String | Null)

            * Include **only** if explicitly found in the source text.
            * Otherwise, set to `null`. Do not generate explanations.

            **Quality Assurance Checklist**
            Before returning the JSON, verify:

            1. Is the JSON syntax valid?
            2. Does every object have the `"AI_GENERATED"` tag?
            3. Are all tables/code blocks in `description` formatted as Markdown?
            4. If the extracted answer was missing, is the `"NO ANSWER"` tag applied and the answer index set to default `["0"]`?
            """;

    public AiService(FileService fileService,
            QuestionService questionService,
            PdfProcessorService pdfProcessorService,
            ChatModel chatModel,
            ObjectMapper mapper) {
        this.fileService = fileService;
        this.questionService = questionService;
        this.pdfProcessorService = pdfProcessorService;
        this.chatModel = chatModel;
        this.mapper = mapper;
    }

    public Flux<AiResponseDTO> extractQuestion(String fileId) {
        DbFile file = fileService.getFileEntity(fileId);
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
        String systemMessage = EXTRACT_TEXT_PROMPT + QUESTION_FORMAT_PROMPT;
        String userMessage = "Here is the file content:\n\n" + fileContent;
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemMessage), new UserMessage(userMessage)),
                OpenAiChatOptions.builder().model(TEXT_MODEL).build());
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
                            List.of(new SystemMessage(EXTRACT_PDF_PROMPT + QUESTION_FORMAT_PROMPT),
                                    new UserMessage("Process these images:", mediaList)),
                            OpenAiChatOptions.builder().model(IMAGE_MODEL).build());

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
                .map(question -> {
                    question.setId(null);
                    question.setQuestionBankId(null);
                    return question;
                })
                .collect(Collectors.toList());
        String questionsJson = mapper.writeValueAsString(questions);
        String systemMessage = GENERATE_QUESTION_FROM_HISTORY_PROMPT.formatted(10)
                + "\n\n" + QUESTION_FORMAT_PROMPT;
        String userMessage = "Here is the practice history:\n\n" + questionsJson;
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemMessage), new UserMessage(userMessage)),
                OpenAiChatOptions.builder().model(TEXT_MODEL).build());

        Flux<AiResponseDTO> start = Flux.just(AiResponseDTO.builder().status("start").build());
        Flux<AiResponseDTO> thinking = Flux.just(AiResponseDTO.builder().status("thinking").build());
        Flux<AiResponseDTO> aiStream = streamAndParseJson(prompt);
        Flux<AiResponseDTO> finish = Flux.just(AiResponseDTO.builder().status("finish").build());

        return Flux.concat(start, thinking, aiStream, finish);
    }

    private Flux<AiResponseDTO> extractContentFromPdf(DbFile file) {
        return Flux.<AiResponseDTO>create(sink -> {
            try {
                sink.next(AiResponseDTO.builder().status("analyze").build());
                DbFile parseFile;
                try {
                    parseFile = fileService.getParseFileEntity(file);
                } catch (Exception e) {
                    List<ProcessedPageDTO> pages = pdfProcessorService.splitePages(file);
                    pages.forEach(page -> System.out.println(page));
                    pages = classifyTextDensity(pages);
                    pages.forEach(page -> System.out.println(page));
                    pages = extractContent(pages);
                    pages.forEach(page -> System.out.println(page));
                    sink.next(AiResponseDTO.builder().status("thinking").build());
                }
                sink.next(AiResponseDTO.builder().status("finish").build());
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public List<ProcessedPageDTO> classifyTextDensity(List<ProcessedPageDTO> pages) {
        List<ProcessedPageDTO> imagePages = new ArrayList<>();
        List<Integer> imageIndices = new ArrayList<>();
        List<Media> mediaList = new ArrayList<>();

        for (int i = 0; i < pages.size(); i++) {
            ProcessedPageDTO page = pages.get(i);
            if (page.getImage() != null) {
                imagePages.add(page);
                imageIndices.add(i);
                mediaList.add(new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(page.getImage())));
            }
        }
        if (mediaList.isEmpty()) {
            return pages;
        }

        UserMessage userMessage = new UserMessage(IMAGE_SORTER_INSTRUCTIONS, mediaList); // Using static final constant
        Prompt prompt = new Prompt(userMessage,
                OpenAiChatOptions.builder().model(IMAGE_MODEL).build());
        System.out.println("Start Classification");
        ChatResponse response = chatModel.call(prompt);

        if (response != null && response.getResult() != null) {
            Generation generation = response.getResult();
            String content = generation.getOutput().getContent();
            System.out.println("Classification Response: ");
            System.out.println(content);
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<TextDensityResponseDTO> results = mapper.readValue(content,
                        new TypeReference<List<TextDensityResponseDTO>>() {
                        });
                for (TextDensityResponseDTO res : results) {
                    if (res.index() >= 0 && res.index() < imagePages.size()) {
                        ProcessedPageDTO p = imagePages.get(res.index());

                        try {
                            p.setTextDensity(ProcessedPageDTO.TextDensity.valueOf(res.density().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            p.setTextDensity(ProcessedPageDTO.TextDensity.MEDIUM); // Fallback
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse AI response: " + content);
                e.printStackTrace();
                // Fallback to defaults if parsing fails
            }
        }

        return pages;
    }

    public List<ProcessedPageDTO> extractContent(List<ProcessedPageDTO> pages) {
        StringBuilder textBuffer = new StringBuilder();
        List<Media> mediaBuffer = new ArrayList<>();
        List<ProcessedPageDTO> currentBatchPages = new ArrayList<>();
        double currentBatchCost = 0;
        int imageIndex = 0; // Track image index within the current batch
        final double BUCKET_LIMIT = 4000.0;

        for (ProcessedPageDTO page : pages) {
            double itemCost = calculateCost(page);
            if (currentBatchCost + itemCost > BUCKET_LIMIT) {
                if (currentBatchCost > 0) {
                    processBatch(textBuffer, mediaBuffer, currentBatchPages);
                }
                textBuffer.setLength(0);
                mediaBuffer.clear();
                currentBatchPages.clear();
                currentBatchCost = 0;
                imageIndex = 0; // Reset image index for the new batch
            }
            currentBatchCost += itemCost;
            currentBatchPages.add(page);
            if (page.getImage() != null) {
                imageIndex++;
                textBuffer.append("\n[[IMAGE_").append(imageIndex).append("]]\n");
                mediaBuffer.add(new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(page.getImage())));
            } else if (page.getText() != null) {
                textBuffer.append(page.getText()).append("\n");
            }
        }

        if (currentBatchCost > 0) {
            processBatch(textBuffer, mediaBuffer, currentBatchPages);
        }
        return pages;
    }

    private double calculateCost(ProcessedPageDTO page) {
        if (page.getImage() != null) {
            ProcessedPageDTO.TextDensity density = page.getTextDensity();
            if (density == null)
                density = ProcessedPageDTO.TextDensity.MEDIUM; // Default

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

    private void processBatch(StringBuilder textBuffer, List<Media> mediaBuffer, List<ProcessedPageDTO> batchPages) {
        if (batchPages.isEmpty())
            return;
        try {
            UserMessage userMessage = new UserMessage(textBuffer.toString(), new ArrayList<>(mediaBuffer));
            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(PDF_TRANSCRIPT_PROMPT), userMessage),
                    OpenAiChatOptions.builder().model(IMAGE_MODEL).build());

            System.out.println("Start Batch Extraction");
            ChatResponse response = chatModel.call(prompt);
            String content = null;
            if (response != null && response.getResult() != null) {
                content = response.getResult().getOutput().getContent();
                System.out.println("Batch Extraction Response: ");
                System.out.println(content);
            }

            if (content != null && !content.isEmpty()) {
                String[] pageParts = content.split("<\\|PAGE_SPLIT_TOKEN\\|>");
                for (int i = 0; i < batchPages.size(); i++) {
                    if (i < pageParts.length) {
                        batchPages.get(i).setText(pageParts[i].trim());
                    } else {
                        batchPages.get(i).setText("[AI ERROR: No Content Generated]");
                    }
                }
            } else {
                for (int i = 0; i < batchPages.size(); i++) {
                    batchPages.get(i).setText("[AI ERROR: No Content Generated]");
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing batch: " + e.getMessage());
            e.printStackTrace();
            for (int i = 0; i < batchPages.size(); i++) {
                batchPages.get(i).setText("[AI ERROR: " + e.getMessage() + "]");
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
