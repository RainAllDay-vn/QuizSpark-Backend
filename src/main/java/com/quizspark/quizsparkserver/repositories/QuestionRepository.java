package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}
