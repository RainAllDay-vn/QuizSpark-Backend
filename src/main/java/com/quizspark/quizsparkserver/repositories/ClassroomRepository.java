package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    boolean existsByJoinCode(String joinCode);

    java.util.Optional<Classroom> findByJoinCode(String joinCode);
}
