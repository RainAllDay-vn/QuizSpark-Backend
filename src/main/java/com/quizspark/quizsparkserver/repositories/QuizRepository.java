package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID>, JpaSpecificationExecutor<Quiz> {
}
