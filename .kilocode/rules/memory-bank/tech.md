# Technology Stack: QuizSpark Server

This document outlines the technologies, libraries, and tools used in the QuizSpark Server.

## Core Technologies

-   **Language**: Java 17
-   **Framework**: Spring Boot 3.5.6
-   **Build Tool**: Gradle

## Database & Storage

-   **Production Database**: PostgreSQL (`org.postgresql:postgresql`)
-   **Development/Test Database**: H2 Database (`com.h2database:h2`)
-   **ORM**: Spring Data JPA (Hibernate)
-   **File Storage**: Stored as `byte[]` in PostgreSQL (`DbFile` entity).

## Security & Authentication

-   **Framework**: Spring Security
-   **Authentication Mechanisms**:
    -   JWT (JSON Web Tokens) - Custom filter implementation.
    -   Firebase Admin SDK (`com.google.firebase:firebase-admin:9.7.0`) - Potentially for token verification or external auth integration.

## AI & Machine Learning

-   **Framework**: Spring AI (`org.springframework.ai:spring-ai-openai-spring-boot-starter`)
-   **Models**:
    -   **Text**: `gemini-3-pro-high` (via OpenAI compatible API)
    -   **Vision**: `gemini-3-flash`
    -   **Chat**: Supports Gemini 3 Pro High, Gemini 3 Flash, and Claude Opus 4.5 Thinking via `ChatService`.
-   **PDF Processing**: Apache PDFBox (`org.apache.pdfbox:pdfbox:2.0.30`) for rendering PDF pages to images.

## API & Communication

-   **REST**: Spring Web MVC.
-   **Reactive Streams**: Spring WebFlux (`org.springframework.boot:spring-boot-starter-webflux`) - Used heavily for streaming AI responses (SSE).
-   **Validation**: Spring Validation (Hibernate Validator).

## Testing

-   **Framework**: JUnit 5
-   **Integration Testing**: Spring Boot Test (`@SpringBootTest`)
-   **Security Testing**: Spring Security Test
-   **Reactive Testing**: Reactor Test

## Configuration Management

-   **Environment Variables**: `me.paulschwarz:spring-dotenv:4.0.0` for loading `.env` files.
-   **Properties**: `application.properties`/`application.yml` for Spring config.

## Development Tools

-   **Lombok**: Annotation processing for boilerplate code reduction.
-   **DevTools**: Spring Boot DevTools for hot reloading.
-   **Docker**: Dockerfile present for containerization.