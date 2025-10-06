package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> getUserDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("userCount", userRepository.findAll().size());
        return status;
    }

    public Optional<User> getUser(String username, String password) {
        User user = userRepository.getUsersByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public Optional<User> saveUser(User user) {
        if(userRepository.getUsersByUsername(user.getUsername()) != null) return Optional.empty();
        user.setId(UUID.randomUUID());
        return Optional.of(userRepository.save(user));
    }
}
