package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Quiz;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.CollectionRepository;
import com.quizspark.quizsparkserver.repositories.QuizRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuizService {
    private final CollectionRepository collectionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Autowired
    public QuizService(CollectionRepository collectionRepository, QuizRepository quizRepository, UserRepository userRepository) {
        this.collectionRepository = collectionRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    public List<Collection> getCollectionByUser(String userId) {
        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        return user.map(User::getCollections).orElse(null);
    }

    public List<Quiz> getQuizByCollection(String collectionId) {
        Optional<Collection> collection = collectionRepository.findById(UUID.fromString(collectionId));
        return collection.map(Collection::getQuizzes).orElse(null);
    }
}
