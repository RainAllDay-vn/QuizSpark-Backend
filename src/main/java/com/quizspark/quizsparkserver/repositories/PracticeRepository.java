package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PracticeRepository extends JpaRepository<Practice, UUID> {
    @Query("SELECT p " +
            "FROM Practice p " +
            "WHERE p.user = :user " +
                "AND (:isClosed IS NULL OR p.closed = :isClosed)")
    Page<Practice> findByUser(User user, Boolean isClosed, Pageable pageable);
}
