package com.quizspark.quizsparkserver.config;

import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

@Configuration
public class TestDataConfig {

    @Bean
    CommandLineRunner testDataInitializer(UserRepository userRepository) {
        return args -> {
            userRepository.deleteAll();
            List<User> users = List.of(
                    new User(UUID.randomUUID(), "alice", "password123"),
                    new User(UUID.randomUUID(), "bob", "securepass"),
                    new User(UUID.randomUUID(), "charlie", "charlie123"),
                    new User(UUID.randomUUID(), "diana", "diana456")
            );
            userRepository.saveAll(users);
            System.out.println("✅ Test data inserted into H2");
        };
    }
}
