package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.banks.QuestionBankCreationDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankUpdateDTO;
import com.quizspark.quizsparkserver.dto.questions.*;
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

    public List<QuestionBankDTO> getPublicQuestionBanks() {
        return bankRepository.findAllByAccess(QuestionBank.Access.PUBLIC)
                .stream()
                .filter(bank -> bank.getStatus() == QuestionBank.Status.PUBLISHED)
                .map(QuestionBankDTO::new)
                .toList();
    }

    public List<QuestionBankDTO> getQuestionBankByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return bankRepository.findQuestionBankByUser(user)
                .stream()
                .map(QuestionBankDTO::new)
                .toList();
    }

    public List<QuestionBankDTO> getAdminBank() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) {
            return bankRepository.findAdminQuestionBank()
                    .stream()
                    .map(QuestionBankDTO::new)
                    .toList();
        } else {
            return List.of();
        }
    }

    public QuestionBank getQuestionBankEntityById(String bankId) {
        Optional<QuestionBank> bankOptional = bankRepository.findById(UUID.fromString(bankId));
        if (bankOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        QuestionBank bank = bankOptional.get();
        if (bank.getAccess() == QuestionBank.Access.PUBLIC) return bank;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getRole().equals("ROLE_ADMIN")) return bank;
        if(!bank.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
        return bank;
    }

    public QuestionBankDTO getQuestionBankById(String bankId) {
        return new QuestionBankDTO(getQuestionBankEntityById(bankId));
    }

    private boolean canBeEdited(QuestionBank questionBank){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (questionBank.getUser()==null && !user.getRole().equals("ROLE_ADMIN"))
            return false;
        if (questionBank.getUser()!=null && !questionBank.getUser().equals(user))
            return false;
        return true;
    }

    public QuestionBankDTO saveQuestionBank(QuestionBankCreationDTO bankCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank questionBank = bankCreationDTO.toQuestionBank(user);
        if (!canBeEdited(questionBank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to save this question bank");
        questionBank = bankRepository.save(questionBank);
        return new QuestionBankDTO(questionBank);
    }

    public QuestionBankDTO savePublicQuestionBank(QuestionBankCreationDTO bankCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.getRole().equals("ROLE_ADMIN"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to save this question bank");
        QuestionBank questionBank = bankCreationDTO.toQuestionBank(null);
        questionBank = bankRepository.save(questionBank);
        return new QuestionBankDTO(questionBank);
    }

    public QuestionBankDTO updateQuestionBank(String bankId, QuestionBankUpdateDTO questionBankUpdateDTO) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        if (!canBeEdited(questionBank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this question bank");
        questionBankUpdateDTO.updateQuestionBank(questionBank);
        questionBank = bankRepository.save(questionBank);
        return new QuestionBankDTO(questionBank);
    }

    public void deleteQuestionBank(String bankId) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        if (!canBeEdited(questionBank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this question bank");
        bankRepository.delete(questionBank);
    }

    public Question getQuestionEntityById(String questionId) {
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

    private boolean canBeEdited(Question question){
        QuestionBank questionBank = question.getQuestionBank();
        return canBeEdited(questionBank);
    }

    public QuestionDTO saveQuestion(String bankId, QuestionCreationDTO questionCreationDTO) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        Question question = questionCreationDTO.toQuestion();
        if (!canBeEdited(question))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to save this question");
        question.setQuestionBank(questionBank);
        question = questionRepository.save(question);
        return new QuestionDTO(question);
    }

    public QuestionDTO updateQuestion(String questionId, QuestionUpdateDTO questionUpdateDTO) {
        Question question = getQuestionEntityById(questionId);
        if (!canBeEdited(question))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this question");
        questionUpdateDTO.updateQuestion(question);
        question = questionRepository.save(question);
        return new QuestionDTO(question);
    }

    @Transactional
    public List<QuestionDTO> overwriteQuestions(String bankId, List<QuestionCreationDTO> questionCreationDTOS){
        QuestionBank bank = getQuestionBankEntityById(bankId);
        if (!canBeEdited(bank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to overwrite questions in this bank");
        List<Question> questions = questionCreationDTOS.stream()
                .map(QuestionCreationDTO::toQuestion)
                .toList();
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

        QuestionBank savedBank = bankRepository.save(bank);
        return savedBank.getQuestions()
                .stream()
                .map(QuestionDTO::new)
                .toList();
    }

    public void deleteQuestion(String questionId) {
        Question question = getQuestionEntityById(questionId);
        if (!canBeEdited(question))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this question");
        questionRepository.delete(question);
    }

    public QuestionComment getCommentEntityById(String commentId) {
        Optional<QuestionComment> commentOptional = questionCommentRepository.findById(UUID.fromString(commentId));
        if (commentOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        QuestionComment comment = commentOptional.get();
        if (getQuestionEntityById(comment.getQuestion().getId().toString())==null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not view this comment");
        return comment;
    }

    public List<QuestionCommentDTO> getComments(String questionId) {
        Question question = getQuestionEntityById(questionId);
        List<QuestionComment> comments = question.getComments();
        return comments.stream()
                .map(QuestionCommentDTO::new)
                .toList();
    }

    public QuestionCommentDTO addComment(String questionId, QuestionCommentCreationDTO commentCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Question question = getQuestionEntityById(questionId);
        QuestionComment comment = commentCreationDTO.toComment(user, question);
        comment = questionCommentRepository.save(comment);
        return new QuestionCommentDTO(comment);
    }

    public QuestionCommentDTO updateComment(String commentId, QuestionCommentUpdateDTO commentUpdateDTO) {
        QuestionComment questionComment = getCommentEntityById(commentId);
        commentUpdateDTO.updateComment(questionComment);
        questionComment = questionCommentRepository.save(questionComment);
        return new QuestionCommentDTO(questionComment);
    }
}
