package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.DbFileContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DbFileContentRepository extends JpaRepository<DbFileContent, UUID> {
}