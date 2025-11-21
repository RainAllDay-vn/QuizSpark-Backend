package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    @Query("SELECT pq " +
            "FROM practice_question pq " +
            "WHERE pq.practice.questionBank = :bank " +
            "   AND pq.practice.user = :user")
    List<PracticeQuestion> findByQuestionBankAndUser(@Param("bank") QuestionBank bank, @Param("user") User user);
}
