package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User getUsersByUsername(String username);

    User getUsersByEmail(String email);
}
