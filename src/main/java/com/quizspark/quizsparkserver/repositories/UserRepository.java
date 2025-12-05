package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.dto.UserStatisticDTO;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User getUsersByUsername(String username);

    User getUsersByEmail(String email);

    @Query("SELECT COALESCE(COUNT(qb.id), 0) AS totalQuestionBanks, " +
                "COALESCE(COUNT(pq.id), 0) AS totalQuestionsAnswered, " +
                "COALESCE(SUM(pq.secondsToAnswer), 0) AS totalTimeSpentInSeconds " +
            "FROM users u " +
            "LEFT JOIN QuestionBank qb ON u.id = qb.user.id " +
            "LEFT JOIN PracticeQuestion pq ON u.id = pq.practice.user.id " +
            "WHERE u.id = :userId")
    UserStatisticDTO getUserStatistic(UUID userId);
}
