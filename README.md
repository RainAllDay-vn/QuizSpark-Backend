# QuizSpark Server 🧠⚙️

> **The Intelligent Core of QuizSpark - An AI-Powered Adaptive Learning Platform**

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=springboot)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-green?logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-8.5-blue?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)

## 🚀 Overview

The **QuizSpark Server** is the robust backend engine powering the QuizSpark ecosystem. Built with **Spring Boot** and **Spring AI**, it orchestrates the platform's intelligent features, managing everything from user authentication and file processing to complex AI-driven adaptive learning algorithms.

This repository houses the **RESTful API** that serves the frontend application, handling data persistence, business logic, and integration with advanced AI models (local LLMs via Ollama & cloud-based OpenAI).

## 💡 Key Features

### 🧠 Advanced AI Integration
- **RAG Architecture (Retrieval-Augmented Generation)**: Uses **Qdrant** vector database to provide context-aware AI responses based on user-uploaded documents.
- **Multi-Model Support**: Seamlessly switch between **OpenAI** GPT models and local **Ollama** models for cost-effective and private processing.
- **Smart Document Processing**: Leveraging **Apache PDFBox**, the server extracts and processes text from extensive PDF libraries to generate quizzes and summaries.

### 🔐 Secure & Scalable Architecture
- **Firebase Authentication**: Robust integration with Firebase Admin SDK for secure user management and token verification.
- **Role-Based Access Control**: Spring Security ensures data protection across student and teacher roles.
- **Scalable Data Layer**: Optimized for performance with **PostgreSQL** (production) and H2 (development), managed via Spring Data JPA.

### 📚 Core Services
- **Adaptive Practice Engine**: Algorithms that analyze user performance to serve personalized questions (`PracticeService`).
- **Dynamic Question Bank**: centralized management of AI-generated and user-created questions (`QuestionBankService`).
- **Real-Time Chat Context**: specialized endpoints for the AI tutor chatbot, maintaining conversation history and document context (`ChatService`).
- **Classroom Management**: APIs for creating virtual classrooms, assigning tasks, and tracking student analytics (`ClassroomService`).

## 🛠️ Technology Stack

Built with enterprise-grade Java technologies for reliability and performance.

### **Core Framework**
- **Java 17**: LTS version for stability and modern language features.
- **Spring Boot 3.5.6**: The leading framework for building Java microservices.
- **Spring AI**: Unified interface for interacting with AI models and vector stores.

### **Data & Storage**
- **PostgreSQL**: Robust relational database for primary data storage.
- **Qdrant**: High-performance vector search engine for RAG implementation.
- **H2 Database**: In-memory database for rapid development and testing.

### **DevOps & Build**
- **Gradle**: Powerful build automation tool.
- **Docker**: Containerization for the application and services (Qdrant).
- **Docker Compose**: Orchestration for local development environments.

## 🏗️ Backend Architecture

The server follows a clean **Controller-Service-Repository** layered architecture:

```
src/main/java/com/quizspark/quizsparkserver/
├── api/                # REST Controllers (Endpoints)
│   ├── AiController.java       # AI processing endpoints
│   ├── ChatController.java     # Chatbot interaction
│   └── ...
├── services/           # Business Logic Layer
│   ├── AiFileProcessService.java # RAG & Vector embedding logic
│   ├── PdfProcessorService.java  # PDF text extraction
│   └── ...
├── repositories/       # Data Access Layer (Spring Data JPA)
├── models/             # JPA Entities
├── dto/                # Data Transfer Objects
└── config/             # App Configuration (Security, AI, CORS)
```

## 🐳 Getting Started

Follow these steps to set up the server locally.

### Prerequisites
- **Java 17 Development Kit (JDK)**
- **Docker Desktop** (for Qdrant vector store)
- **Node.js** (optional, if running frontend)

### Local Development

1.  **Clone the repository**
    ```bash
    git clone https://github.com/RainAllDay-vn/QuizSpark-Backend.git
    cd QuizSpark-Backend
    ```

2.  **Start Infrastructure Services**
    Start the Qdrant vector database using Docker Compose:
    ```bash
    docker-compose up -d
    ```

3.  **Configure Environment**
    The application uses `dotenv` for configuration. Create or verify variables in your environment (or a `.env` file if supported):
    ```env
    OPENAI_API_KEY=your_openai_key_here
    OPENAI_BASE_URL=https://api.openai.com/v1 (or your local Ollama URL)
    ```

4.  **Run the Application**
    Use the Gradle wrapper to start the server:
    ```bash
    ./gradlew bootRun
    ```
    The server will start at `http://localhost:8080`.

5.  **Access API Documentation**
    (If Swagger UI is enabled) Open `http://localhost:8080/swagger-ui.html` to explore the endpoints.

### Docker Deployment

1.  **Build the Docker Image**
    ```bash
    docker build -t quizspark-server .
    ```

2.  **Run with Environment Variables**
    ```bash
    docker run -p 8080:8080 \
      -e OPENAI_API_KEY=your_key \
      quizspark-server
    ```

## 🧪 Testing

Run the test suite to ensure system integrity:

```bash
./gradlew test
```

## 👨‍💻 Author

**Long Vu**
