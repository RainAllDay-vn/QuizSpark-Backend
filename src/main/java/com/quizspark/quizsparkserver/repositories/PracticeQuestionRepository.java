package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PracticeQuestionRepository
        extends JpaRepository<PracticeQuestion, PracticeQuestion.PracticeQuestionId> {
    @Query("""
            SELECT pq
            FROM PracticeQuestion pq
            WHERE pq.practice.user = :user
               AND pq.baseQuestion IN :questions
            """)
    List<PracticeQuestion> findByQuestionBankAndUser(@Param("questions") List<Question> questions, @Param("user") User user);

    @Query("""
            SELECT pq
            FROM PracticeQuestion pq
            LEFT JOIN FETCH pq.practice
            WHERE pq.practice.id = :practiceID
               AND pq.id.index = :index
            """)
    Optional<PracticeQuestion> findByPracticeAndIndex(UUID practiceID, int index);

    @Query("""
            SELECT pq
            FROM PracticeQuestion pq
            WHERE pq.practice.user.id = :userId
               AND pq.baseQuestion.questionBank.id = :bankId
            """)
    List<PracticeQuestion> findByUserAndBank(UUID userId, UUID bankId);
}
