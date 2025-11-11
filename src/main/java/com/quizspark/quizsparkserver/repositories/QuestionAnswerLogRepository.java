package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.QuestionAnswerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionAnswerLogRepository
        extends JpaRepository<QuestionAnswerLog, Integer>, JpaSpecificationExecutor<QuestionAnswerLog> {
}
