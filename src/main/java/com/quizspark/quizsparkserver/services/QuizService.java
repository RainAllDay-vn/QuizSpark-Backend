package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Quiz;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.CollectionRepository;
import com.quizspark.quizsparkserver.repositories.QuizRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<Collection> getPublicCollections() {
        return collectionRepository.findAllByAccess(Collection.Access.PUBLIC);
    }

    public List<Collection> getCollectionByUser(String userId) {
        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        return user.map(User::getCollections).orElse(null);
    }

    public Collection getCollectionById(String collectionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Collection> optional = collectionRepository.findById(UUID.fromString(collectionId));
        if (optional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        Collection collection = optional.get();
        if(user.getRole().equals("ROLE_ADMIN")) return collection;
        if(!collection.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this collection");
        return collection;
    }

    public Collection saveCollection(Collection collection) {
        return collectionRepository.save(collection);
    }

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }
}
