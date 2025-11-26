package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, PracticeQuestion.PracticeQuestionId> {
    @Query("SELECT pq " +
            "FROM PracticeQuestion pq " +
            "WHERE pq.question IN :questions " +
            "   AND pq.practice.user = :user")
    List<PracticeQuestion> findByQuestionsAndUser(List<Question> questions, User user);
}
