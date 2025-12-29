package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.DbFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<DbFile, UUID> {
}
