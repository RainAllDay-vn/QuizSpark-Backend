# Project Context: QuizSpark Server

This document tracks the current status, focus, and immediate next steps for the QuizSpark Server project.

## Current State

The core backend infrastructure is implemented and functional.
-   **Authentication**: Functional (JWT/Firebase).
-   **Core Entities**: User, QuestionBank, Question, Practice, Classroom, Chat, File support are modeled and mapped.
-   **API Layer**: REST controllers exist for all major domains.
-   **AI Services**: Integrated with Spring AI for question extraction (Text/PDF/Image) and generation.
-   **PDF Processing**: Manual and AI-driven PDF processing logic is in place.
-   **Chat**: Real-time streaming chat with session management and file support.

## Current Focus

Initial setup of the architectural documentation (Memory Bank) to enable effective AI collaboration and future development. The project seems conducive to a robust educational platform, and future tasks will likely involve feature refinement, optimization, and frontend integration support.

## Recent Changes
-   Initialized Memory Bank structure (`brief.md`, `product.md`).
-   Analyzed codebase to understand current capabilities.
-   Added `toChatItem` method to `ChatItemDTO` and refactored `ChatService` to use it.
-   Implemented chat request logging in `ChatService` using SLF4J (Spring's default logger) to capture user messages, media counts, and AI responses.
-   Updated `FileDTO` to include a list of `derivedFiles`, ensuring derived files themselves cannot have nesting.
-   Configured physical cascading deletes for `QuestionBank` using Hibernate's `@OnDelete(action = OnDeleteAction.CASCADE)` on dependent entities and join tables. Added programmatic cleanup in `QuestionService` and `TagService` to handle referential integrity with many-to-many associations and soft-deleted questions.

## Next Steps
1.  Complete Memory Bank initialization (`architecture.md`, `tech.md`).
2.  Verify system understanding with the user.
3.  Await further development tasks (e.g., bug fixes, new features, testing).