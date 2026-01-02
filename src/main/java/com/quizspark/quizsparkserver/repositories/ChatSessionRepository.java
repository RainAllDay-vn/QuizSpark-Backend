package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.ChatSession;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);
}
