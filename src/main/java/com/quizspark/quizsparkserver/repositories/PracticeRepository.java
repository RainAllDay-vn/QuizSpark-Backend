package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PracticeRepository extends JpaRepository<Practice, UUID> {
    Page<Practice> findByUser(User user, Pageable pageable);
}
