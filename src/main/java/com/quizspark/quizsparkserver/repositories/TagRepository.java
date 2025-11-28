package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findAllByBank(QuestionBank bank);
}
