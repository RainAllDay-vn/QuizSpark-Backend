package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.ChatMessage;
import com.quizspark.quizsparkserver.models.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);

    List<ChatMessage> findBySessionOrderByMessageIndexAsc(ChatSession session);

    Long countBySession(ChatSession session);

    List<ChatMessage> findBySessionAndMessageIndexGreaterThanEqual(ChatSession session, Integer index);
}
