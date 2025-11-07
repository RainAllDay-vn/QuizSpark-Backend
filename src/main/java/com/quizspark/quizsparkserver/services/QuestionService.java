package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.QuestionRepository;
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
public class QuestionService {
    private final QuestionBankRepository bankRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Autowired
    public QuestionService(QuestionBankRepository bankRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public List<QuestionBank> getPublicQuestionBanks() {
        return bankRepository.findAllByAccess(QuestionBank.Access.PUBLIC);
    }

    public List<QuestionBank> getQuestionBankByUser(String userId) {
        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        return user.map(User::getQuestionBanks).orElse(null);
    }

    public QuestionBank getQuestionBankById(String collectionId) {
        Optional<QuestionBank> optional = bankRepository.findById(UUID.fromString(collectionId));
        if (optional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        QuestionBank questionBank = optional.get();
        if (questionBank.getAccess() == QuestionBank.Access.PUBLIC) return questionBank;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) return questionBank;
        if(!questionBank.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this collection");
        return questionBank;
    }

    public QuestionBank saveQuestionBank(QuestionBank questionBank) {
        return bankRepository.save(questionBank);
    }

    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
}
