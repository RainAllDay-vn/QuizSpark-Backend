package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.QuestionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionCommentRepository extends JpaRepository<QuestionComment, UUID> {
}
