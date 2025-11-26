package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    @Query("SELECT pq " +
            "FROM practice_question pq " +
            "WHERE pq.question IN :questions " +
            "   AND pq.practice.user = :user")
    List<PracticeQuestion> findByQuestionsAndUser(List<Question> questions, User user);
}
