package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionBankRepository extends JpaRepository<QuestionBank, UUID> {
    List<QuestionBank> findAllByAccess(QuestionBank.Access access);

    List<QuestionBank> findQuestionBankByUser(User user);

    @Query("SELECT qb FROM question_bank qb WHERE qb.user IS NULL")
    List<QuestionBank> findAdminQuestionBank();
}
