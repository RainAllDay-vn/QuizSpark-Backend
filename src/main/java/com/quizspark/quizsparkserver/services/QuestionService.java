package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.QuestionAnswerLog;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.QuestionAnswerLogRepository;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.QuestionRepository;
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
    private final QuestionAnswerLogRepository logRepository;

    @Autowired
    public QuestionService(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository,
            QuestionAnswerLogRepository logRepository
    ) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
        this.logRepository = logRepository;
    }

    public List<QuestionBank> getPublicQuestionBanks() {
        return bankRepository.findAllByAccess(QuestionBank.Access.PUBLIC)
                .stream()
                .filter(bank -> bank.getStatus() == QuestionBank.Status.PUBLISHED)
                .toList();
    }

    public List<QuestionBank> getQuestionBankByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return bankRepository.getQuestionBankByUser(user);
    }

    public List<QuestionBank> getAdminBank() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) {
            return bankRepository.findAll()
                    .stream()
                    .filter(bank -> bank.getUser() == null)
                    .toList();
        } else {
            return List.of();
        }
    }

    public QuestionBank getQuestionBankById(String bankId) {
        Optional<QuestionBank> optional = bankRepository.findById(UUID.fromString(bankId));
        if (optional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        QuestionBank questionBank = optional.get();
        if (questionBank.getAccess() == QuestionBank.Access.PUBLIC) return questionBank;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) return questionBank;
        if(!questionBank.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this collection");
        return questionBank;
    }

    public void saveQuestionBank(QuestionBank questionBank) {
        bankRepository.save(questionBank);
    }

    public Question getQuestionById(String questionId) {
        Optional<Question> question = questionRepository.findById(UUID.fromString(questionId));
        if (question.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole().equals("ROLE_ADMIN")) return question.get();
        QuestionBank bank = question.get().getQuestionBank();
        if (bank.getUser().getId().equals(user.getId())) return question.get();
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }

    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    public void saveQuestionAnswerLog(QuestionAnswerLog log) {
        logRepository.save(log);
    }
}
