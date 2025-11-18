package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Practice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PracticeRepository extends JpaRepository<Practice, UUID> {
}
