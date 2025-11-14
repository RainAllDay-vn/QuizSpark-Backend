package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.QuestionAnswerLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAnswerLogRepository extends JpaRepository<QuestionAnswerLog, Integer>{
}
