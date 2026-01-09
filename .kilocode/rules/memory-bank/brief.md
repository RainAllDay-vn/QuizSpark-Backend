# Project Brief: QuizSpark Server

QuizSpark Server is the backend infrastructure for the QuizSpark platform, an intelligent educational tool designed to generate, manage, and practice quizzes. It leverages AI to extract questions from various file formats (Text, PDF, Images) and provides a comprehensive system for classroom management, collaborative learning, and adaptive practice.

## Core Requirements

- **User Management**: Authentication and authorization (using JWT/Firebase), user profiles, and role-based access (Student, Teacher/Admin).
- **Question Bank Management**: Create, update, delete, and organize collections of questions. Support for public and private banks.
- **Question Management**: Support for multiple question types (Single Answer, Multiple Answer, Fill-in-the-blank, Open Answer).
- **AI Integration**:
    - **Extraction**: Extract questions from uploaded files (Text, PDF, Images) using AI models (OpenAI/Spring AI).
    - **Generation**: Generate adaptive questions based on user practice history to target weak areas.
    - **Chat**: Interactive AI chat for educational assistance, supporting text and file inputs.
- **Practice Mode**:
    - **Sessions**: Customizable practice sessions (size, shuffle, tags).
    - **Adaptive Learning**: Track user performance and provide statistics.
    - **Anonymous Practice**: Allow practice without login (limited features).
- **Classroom System**:
    - **Management**: Create and manage classrooms.
    - **Membership**: Invite users join via code.
- **File Management**: Upload and manage files associated with questions and chats.

## Project Goals

- **Scalability**: Designed to handle multiple users and large datasets of questions.
- **Intelligence**: deeply integrated AI for content generation and student assistance.
- **Flexibility**: Support for various input formats and question types.
- **Community**: Foster collaborative learning through public question banks and classrooms.