package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("SELECT q FROM question q LEFT JOIN FETCH q.questionBank WHERE q.id = :id")
    @NonNull
    Optional<Question> findById(@NonNull UUID id);
}
