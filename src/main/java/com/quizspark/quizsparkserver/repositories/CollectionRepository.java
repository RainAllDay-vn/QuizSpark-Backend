package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID>, JpaSpecificationExecutor<Collection> {
}
