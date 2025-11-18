package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.dto.practice.QuestionStatisticDTO;
import com.quizspark.quizsparkserver.models.PracticeQuestion;
import com.quizspark.quizsparkserver.models.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    @Query("SELECT pq, pq.question FROM practice_question pq WHERE pq.practice.questionBank = :bank")
    List<PracticeQuestion> findByQuestionBankId(@Param("bank") QuestionBank bank);
}
