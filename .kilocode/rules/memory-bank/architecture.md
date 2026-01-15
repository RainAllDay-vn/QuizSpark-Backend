# System Architecture: QuizSpark Server

QuizSpark Server is built as a monolithic Java application using Spring Boot. It follows a classic layered architecture (Controller - Service - Repository) to separate concerns and ensure maintainability.

## System Components

### 1. API Layer (Controllers)
Located in `src/main/java/com/quizspark/quizsparkserver/api/`.
-   **Endpoints**: RESTful APIs for client interaction.
-   **Responsibilities**: Request validation, input parsing, and delegating business logic to services.
-   **Key Controllers**:
    -   `AuthController`/`UserController`: Authentication & user profile.
    -   `QuestionController`/`QuestionBankController`: Content management.
    -   `PracticeController`: Practice session execution.
    -   `AiController`: Interfacing with AI for extraction/generation.
    -   `ChatController`: handling real-time AI study chat.
    -   `ClassroomController`: Classroom management and joining.

### 2. Business Logic Layer (Services)
Located in `src/main/java/com/quizspark/quizsparkserver/services/`.
-   **Responsibilities**: Core application logic, transaction management, and orchestration.
-   **Key Services**:
    -   `AiService`: Heaviest logic component. Handles prompts, interactions with Spring AI (OpenAI), PDF processing strategies (text density classification, batching), and result parsing.
    -   `ChatService`: Manages chat sessions, message history, and interfacing with various LLM models (Gemini, Claude) via Spring AI.
    -   `PracticeService`: Logic for generating practice sets, calculating statistics, and recording progress.
    -   `PdfProcessorService`: PDF to image conversion (Apache PDFBox) and page splitting.

### 3. Data Access Layer (Repositories)
Located in `src/main/java/com/quizspark/quizsparkserver/repositories/`.
-   **Technology**: Spring Data JPA (Hibernate).
-   **Responsibilities**: CRUD operations and database queries.
-   **Entities**: Mapped in `src/main/java/com/quizspark/quizsparkserver/models/`.

### 4. Database
-   **Primary DB**: PostgreSQL (production), H2 (dev/test).
-   **Schema**:
    -   `users`: User accounts.
    -   `question_bank` & `question` & `question_tag`: Content content.
    -   `practice` & `practice_question` & `practice_statistic`: User activity and results.
    -   `classroom` & `classroom_member`: Social structure.
    -   `chat_session` & `chat_message`: Chat history.
    -   `db_file`: File storage (likely binary data in DB for this scale, or mapping to external storage).

## Key Technical Decisions

-   **Spring AI**: Chosen for centralized AI orchestration. Allows switching between models (OpenAI, Gemini, Claude, etc.) via configuration.
-   **Server-Sent Events (SSE)**: Used in `AiController` and `ChatController` (`Flux<ReturnType>`) to stream AI responses to the client, improving perceived latency.
-   **PDF Processing Pipeline**:
    -   Complex logic in `AiService` to handle huge PDFs.
    -   Strategy: Split -> Convert to Images -> Classify Density (High/Med/Low) -> Batch -> Send to Vision Model -> Reconstruct text.
-   **JWT & Firebase**: Uses Firebase for potential auth delegation or just standard JWT for session management (needs to be confirmed from `WebSecurityConfig` analysis, currently appearing mixed).

## Data Flow (Question Extraction)

1.  **Upload**: User uploads PDF via `FileController`.
2.  **Request**: Client requests AI extraction on file ID.
3.  **Process** (`AiService`):
    -   File retrieved from DB.
    -   Converted to images.
    -   Images classified for text density (to estimate token cost).
    -   Batched and sent to Vision LLM.
    -   Structured JSON (QuestionDTO) returned.
4.  **Stream**: Results streamed back to client as they are generated.

## Directory Structure

`src/main/java/com/quizspark/quizsparkserver/`
-   `api/`: REST Controllers
-   `config/`: Configuration (Security, Firebase)
-   `dto/`: Data Transfer Objects
-   `models/`: JPA Entities
-   `repositories/`: Spring Data Interfaces
-   `services/`: Business Logic
-   `utils/`: Helper classes (JWT)