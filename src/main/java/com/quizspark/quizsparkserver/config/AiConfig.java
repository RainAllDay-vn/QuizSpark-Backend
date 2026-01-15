package com.quizspark.quizsparkserver.config;

import com.quizspark.quizsparkserver.dto.chat.ModelDTO;
import com.quizspark.quizsparkserver.dto.chat.ModelDTO.ModelCharacteristic;

import java.util.List;

public class AiConfig {
    // Model Names
    public static final String TEXT_MODEL = "gemini-3-pro-high";
    public static final String IMAGE_MODEL = "gemini-3-flash";
    public static final String CHAT_DEFAULT_MODEL = "deepseek-ai/DeepSeek-V3.2-TEE";

    public static final List<ModelDTO> AVAILABLE_CHAT_MODELS = List.of(
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

    // Prompts - Adaptive Learning (AiService)
    public static final String ADAPTIVE_GENERATION_PROMPT = """
            # Role
            You are an Adaptive Learning Engine. Your goal is to analyze a user's performance and provide a continuous stream of tailored educational content.
            
            # Input
            1. **Scratchpad**: Your own summary of the user's progress from the previous turn. Use this to maintain context.
            2. **Recent History**: The last 5-10 questions the user attempted, including their description, choices, and whether they were correct.
            
            # Task
            1. **Update Scratchpad**: Analyze the recent history. Identify persistent weaknesses, improving trends, or areas of mastery. Write a concise summary (the "scratchpad") for your future self.
            2. **Generate Questions**: Generate %d new questions that:
               - Target identified weak spots (Reinforcement).
               - Introduce slightly more difficult variations of mastered concepts (Progression).
               - Maintain the core themes of the original Question Bank.
            
            # Output Format
            Return valid JSON only, following this structure:
            {
              "scratchPad": "Your updated summary here...",
              "questions": [
                {
                  "questionType": "SINGLE_ANSWER",
                  "tags": ["AI_GENERATED"],
                  "description": "Question text...",
                  "choices": ["A", "B", "C", "D"],
                  "answer": ["0"]
                }
              ]
            }
            """;

    // Prompts - Extraction & Processing (AiFileProcessService)
    public static final String EXTRACT_TEXT_PROMPT = """
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

    public static final String EXTRACT_PDF_PROMPT = """
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
            * **Math/Equations:** Convert visual mathematical notation (fractions, integrals, superscripts) into LaTeX format (e.g., `$\\frac{1}{2}$`).
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

    public static final String GENERATE_QUESTION_FROM_HISTORY_PROMPT = """
            # Role
            You are an Intelligent Adaptive Learning Engine. Your goal is to analyze a user's historical performance data and dynamically generate a new curriculum of %1$d questions to optimize their learning path.

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
               - **Low Mastery (< 60%%):** The user struggles here. These areas require **Reinforcement**.
               - **High Mastery (> 85%%):** The user is proficient. These areas require **Challenge/Deepening**.
               - **Moderate Mastery (60-85%%):** The user is inconsistent. These areas require **Practice**.

            # Generation Strategy
            Generate exactly %1$d new questions. You must determine the best balance of questions based on the analysis above:

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
            - Generate exactly %1$d questions.
            - Maintain the subject matter context provided in the input `tags`.
            - Ensure all generated questions are factually correct.
            """;

    public static final String IMAGE_SORTER_INSTRUCTIONS = """
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

    public static final String PDF_TRANSCRIPT_PROMPT = """
            ### Input Structure (CRITICAL)
            Each page in the input follows this structure:
            - **Optional Image Placeholder:** `[[IMAGE_N]]` appears if the page requires vision processing (N = sequential image number)
            - **Extracted Text:** Raw text extracted programmatically from the PDF page (may be incomplete, malformed, or empty)
            - **Page Delimiter:** `---` marks the end of each page

            **Image Mapping:** `[[IMAGE_N]]` corresponds strictly to the **Nth image attachment** provided in this request.

            ### Processing Logic
            Process the input stream sequentially, page by page. Do not summarize. Do not skip content.

            #### 1. Page Delimiting (STRICT REQUIREMENT)
            - **Mandatory Split:** You **MUST** output the exact token `---` at the very end of your processed content for every single input page.
            - **Preserve Boundaries:** Content from one page must NOT be merged with content from another page. If text is split across pages (e.g., "The coefficient of" on page 1, "friction is" on page 2), output them separately on their respective pages.
            - **Empty Pages:** If an input page contains no meaningful content, output **only** `---`.
            - **Constraint:** Do NOT use `---` for any other purpose than page separation. Use other delimiters like `***` or `___` if needed for horizontal rules.

            #### 2. Per-Page Processing Strategy
            For each page, you receive **both** an image (if `[[IMAGE_N]]` is present) and extracted text. Use this dual-input strategy:

            * **If `[[IMAGE_N]]` is present:**
              - The extracted text may be incomplete or inaccurate (OCR artifacts, missing columns, broken tables).
              - **Use the image as the authoritative source** for transcription.
              - Cross-reference with the extracted text to verify your OCR and catch edge cases.
              - Apply "Image Input Handling" rules below.

            * **If no image placeholder exists:**
              - The page is text-only (no visual elements requiring OCR).
              - **Use the extracted text as the primary source.**
              - Apply "Text Input Handling" rules to clean and normalize.

            #### 3. Visual Annotation Handling (Handwriting & Markups)
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

            #### 4. Text Input Handling (When no image present)
            - **Formatting:** Clean up raw text by fixing spacing, indentation, and broken line breaks caused by PDF extraction.
            - **Structure:** Apply semantic Markdown (e.g., convert "Chapter 1" to `## Chapter 1`, bold key terms).
            - **Preserve Content:** Do not add or remove information. Only normalize formatting.

            #### 5. Image Input Handling (When `[[IMAGE_N]]` is present)
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
            - **Extracted Text Mismatch:** If the image shows different content than the extracted text, trust the image.
            - **Partial Content at Page Boundaries:** Output partial content exactly as it appears on that page. Do NOT attempt to complete or merge with adjacent pages.
            """;

    public static final String QUESTION_FORMAT_PROMPT = """
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
                "choices": ["choice_1", "choice_2"]
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

            **Quality Assurance Checklist**
            Before returning the JSON, verify:

            1. Is the JSON syntax valid?
            2. Does every object have the `"AI_GENERATED"` tag?
            3. Are all tables/code blocks in `description` formatted as Markdown?
            4. If the extracted answer was missing, is the `"NO ANSWER"` tag applied and the answer index set to default `["0"]`?
            """;

    // Prompts - Chat (ChatService)
    public static final String CHAT_SYSTEM_PROMPT = """
            You are a helpful educational assistant for QuizSpark, an intelligent study platform.
            
            ## Context Awareness
            Users may provide contextual information from their workspace. When you see context markers like [WORKSPACE FILE], this indicates content the user is currently viewing or working with on the QuizSpark platform. Their questions are likely related to this context, so prioritize it when formulating your responses.
            
            Context types you may encounter:
            - **WORKSPACE FILE**: A document or file the user is currently viewing/editing in their workspace. Treat this as their primary study material for the conversation.
            
            When context is provided, focus your answers on that material unless the user explicitly asks about something else.
            """;
}
