package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
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

    @Autowired
    public QuestionService(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository
    ) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
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
        Optional<QuestionBank> bankOptional = bankRepository.findById(UUID.fromString(bankId));
        if (bankOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        QuestionBank bank = bankOptional.get();
        if (bank.getAccess() == QuestionBank.Access.PUBLIC) return bank;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) return bank;
        if(!bank.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
        return bank;
    }

    public void saveQuestionBank(QuestionBank questionBank) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN") || questionBank.getUser().equals(user)) {
            bankRepository.save(questionBank);
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    public void deleteQuestionBank(QuestionBank questionBank) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN") || questionBank.getUser().equals(user)) {
            bankRepository.delete(questionBank);
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    public Question getQuestionById(String questionId) {
        Optional<Question> questionOptional = questionRepository.findById(UUID.fromString(questionId));
        if (questionOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        Question question = questionOptional.get();
        if (question.getQuestionBank().getAccess()==QuestionBank.Access.PUBLIC) return question;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole().equals("ROLE_ADMIN")) return question;
        QuestionBank bank = question.getQuestionBank();
        if (bank.getUser().getId().equals(user.getId())) return question;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    public void saveQuestion(Question question) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN") || question.getQuestionBank().getUser().equals(user)) {
            questionRepository.save(question);
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    public void saveAllQuestion(List<Question> questions) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questions.get(0).getQuestionBank();
        if(user.getRole().equals("ROLE_ADMIN") || bank.getUser().equals(user)) {
            questionRepository.saveAll(questions);
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    public void deleteQuestion(Question question) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN") || question.getQuestionBank().getUser().equals(user)) {
            questionRepository.delete(question);
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    public void deleteAllQuestion(List<Question> questions) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questions.get(0).getQuestionBank();
        if(user.getRole().equals("ROLE_ADMIN") || bank.getUser().equals(user)) {
            questionRepository.deleteAll(questions);
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }
}
