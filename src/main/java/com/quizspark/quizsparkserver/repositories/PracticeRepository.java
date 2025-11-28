package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Practice;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeRepository extends JpaRepository<Practice, UUID> {
    List<Practice> findByUser(User user);
}
