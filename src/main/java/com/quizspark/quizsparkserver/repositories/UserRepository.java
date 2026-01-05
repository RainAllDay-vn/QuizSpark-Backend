package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.dto.repositories.PracticeStatisticsDTO;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
        User getUsersByUsername(String username);

        User getUsersByEmail(String email);

        @Query("SELECT COALESCE(COUNT(qb.id), 0) " +
                        "FROM users u " +
                        "LEFT JOIN QuestionBank qb ON u.id = qb.user.id " +
                        "WHERE u.id = :userId")
        long getQuestionBankCount(UUID userId);

        @Query("SELECT new com.quizspark.quizsparkserver.dto.repositories.PracticeStatisticsDTO(" +
                        "COALESCE(COUNT(pq.id), 0), " +
                        "COALESCE(SUM(pq.secondsToAnswer), 0)) " +
                        "FROM users u " +
                        "LEFT JOIN PracticeQuestion pq ON u.id = pq.practice.user.id " +
                        "WHERE u.id = :userId")
        PracticeStatisticsDTO getPracticeStatistics(UUID userId);

        java.util.List<User> findTop10ByUsernameContainingIgnoreCaseAndIdNot(String username, UUID id);
}
