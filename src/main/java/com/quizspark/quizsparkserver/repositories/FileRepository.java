package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<DbFile, UUID> {
    Optional<DbFile> findByFileName(String fileName);

    List<DbFile> findByUser(User user);

    List<DbFile> findByUserId(UUID userId);
}
