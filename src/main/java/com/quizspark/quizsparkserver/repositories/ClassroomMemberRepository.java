package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.ClassroomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClassroomMemberRepository extends JpaRepository<ClassroomMember, UUID> {
}
