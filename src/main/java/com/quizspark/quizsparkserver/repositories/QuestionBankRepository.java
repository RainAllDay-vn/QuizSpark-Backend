package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface QuestionBankRepository extends JpaRepository<QuestionBank, UUID>, JpaSpecificationExecutor<QuestionBank> {
    List<QuestionBank> findAllByAccess(QuestionBank.Access access);
}
