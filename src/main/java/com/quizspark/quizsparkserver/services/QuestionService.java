package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.questions.QuestionCommentCreationDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.QuestionCommentRepository;
import com.quizspark.quizsparkserver.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionBankRepository bankRepository;
    private final QuestionRepository questionRepository;
    private final QuestionCommentRepository questionCommentRepository;

    @Autowired
    public QuestionService(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository, QuestionCommentRepository questionCommentRepository) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
        this.questionCommentRepository = questionCommentRepository;
    }

    public List<QuestionBank> getPublicQuestionBanks() {
        return bankRepository.findAllByAccess(QuestionBank.Access.PUBLIC)
                .stream()
                .filter(bank -> bank.getStatus() == QuestionBank.Status.PUBLISHED)
                .toList();
    }

    public List<QuestionBank> getQuestionBankByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return bankRepository.findQuestionBankByUser(user);
    }

    public List<QuestionBank> getAdminBank() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) {
            return bankRepository.findAdminQuestionBank();
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
        bankRepository.save(questionBank);
    }

    public void deleteQuestionBank(QuestionBank questionBank) {
        bankRepository.delete(questionBank);
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
        questionRepository.save(question);
    }

    @Transactional
    public void overwriteQuestions(QuestionBank bank, List<Question> questions){
        Map<String, Tag> newTagsMap = questions.stream()
                .flatMap(question -> question.getTags().stream())
                .collect(Collectors.toMap(Tag::getName, tag -> tag, (tag1, tag2) -> tag1));
        for (Question question: questions) {
            List<Tag> tags = question.getTags();
            tags = tags.stream().map(tag -> newTagsMap.get(tag.getName())).toList();
            question.setTags(tags);
        }
        List<Tag> newTags = new ArrayList<>(newTagsMap.values());
        newTags.forEach(tag -> tag.setBank(bank));
        questions.forEach(question -> question.setQuestionBank(bank));

        bank.getQuestions().clear();
        bank.getTags().clear();
        bank.getQuestions().addAll(questions);
        bank.getTags().addAll(newTags);
    }

    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    public List<QuestionCommentDTO> getComments(String questionId) {
        Question question = getQuestionById(questionId);
        List<QuestionComment> comments = question.getComments();
        return comments.stream()
                .map(QuestionCommentDTO::new)
                .toList();
    }

    public QuestionCommentDTO addComment(String questionId, QuestionCommentCreationDTO commentCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Question question = getQuestionById(questionId);
        QuestionComment comment = commentCreationDTO.toComment(user, question);
        comment = questionCommentRepository.save(comment);
        return new QuestionCommentDTO(comment);
    }
}
