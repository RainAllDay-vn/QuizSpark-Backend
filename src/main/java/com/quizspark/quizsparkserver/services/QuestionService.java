package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.banks.QuestionBankCreationDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankUpdateDTO;
import com.quizspark.quizsparkserver.dto.questions.*;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.PracticeQuestionRepository;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.QuestionCommentRepository;
import com.quizspark.quizsparkserver.repositories.QuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class QuestionService {
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final QuestionBankRepository bankRepository;
    private final QuestionRepository questionRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final QuestionCommentRepository questionCommentRepository;

    public QuestionService(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository,
            PracticeQuestionRepository practiceQuestionRepository,
            QuestionCommentRepository questionCommentRepository) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.questionCommentRepository = questionCommentRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionBankDTO> getPublicQuestionBanks() {
        return bankRepository.findAllByAccess(QuestionBank.Access.PUBLIC)
                .stream()
                .filter(bank -> bank.getStatus() == QuestionBank.Status.PUBLISHED)
                .map(QuestionBankDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionBankDTO> getQuestionBankByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<QuestionBankDTO> body = new ArrayList<>(bankRepository.findQuestionBankByUser(user)
                .stream()
                .map(QuestionBankDTO::new)
                .toList());
        if (user.getRole().equals("ROLE_ADMIN")) {
            body.addAll(getAdminBank());
        }
        return body;
    }

    @Transactional(readOnly = true)
    public List<QuestionBankDTO> getAdminBank() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole().equals("ROLE_ADMIN")) {
            return bankRepository.findAdminQuestionBank()
                    .stream()
                    .map(QuestionBankDTO::new)
                    .toList();
        } else {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public QuestionBank getQuestionBankEntityById(String bankId) {
        Optional<QuestionBank> bankOptional = bankRepository.findById(UUID.fromString(bankId));
        if (bankOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        QuestionBank bank = bankOptional.get();
        if (bank.getAccess() == QuestionBank.Access.PUBLIC)
            return bank;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole().equals("ROLE_ADMIN"))
            return bank;
        if (!bank.getUser().equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
        return bank;
    }

    @Transactional(readOnly = true)
    public QuestionBankDTO getQuestionBankById(String bankId) {
        return new QuestionBankDTO(getQuestionBankEntityById(bankId));
    }

    @Transactional
    public QuestionBankDTO getFullQuestionBankById(String bankId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        List<PracticeQuestion> practiceHistory = practiceQuestionRepository
                .findByUserAndBank(user.getId(), questionBank.getId());
        return new QuestionBankDTO(questionBank)
                .appendQuestions()
                .appendStatistics(practiceHistory);
    }

    public boolean canBeEdited(QuestionBank questionBank) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (questionBank.getUser() == null && !user.getRole().equals("ROLE_ADMIN"))
            return false;
        if (questionBank.getUser() != null && !questionBank.getUser().equals(user))
            return false;
        return true;
    }

    @Transactional
    public QuestionBankDTO saveQuestionBank(QuestionBankCreationDTO bankCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank questionBank = bankCreationDTO.toQuestionBank(user);
        if (!canBeEdited(questionBank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to save this question bank");
        questionBank = bankRepository.save(questionBank);
        return new QuestionBankDTO(questionBank);
    }

    @Transactional
    public QuestionBankDTO savePublicQuestionBank(QuestionBankCreationDTO bankCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.getRole().equals("ROLE_ADMIN"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to save this question bank");
        QuestionBank questionBank = bankCreationDTO.toQuestionBank(null);
        questionBank = bankRepository.save(questionBank);
        return new QuestionBankDTO(questionBank);
    }

    @Transactional
    public QuestionBankDTO updateQuestionBank(String bankId, QuestionBankUpdateDTO questionBankUpdateDTO) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        if (!canBeEdited(questionBank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to update this question bank");
        questionBankUpdateDTO.updateQuestionBank(questionBank);
        questionBank = bankRepository.save(questionBank);
        return new QuestionBankDTO(questionBank);
    }

    @Transactional
    public void deleteQuestionBank(String bankId) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        if (!canBeEdited(questionBank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this question bank");

        // Break associations with Tags manually for ALL questions in the bank.
        // We must query the repository directly because the bank.getQuestions()
        // collection is @SQLRestriction filtered and won't include soft-deleted questions.
        // Soft-deleted questions in the DB still have join table entries that block Tag deletion.
        List<Question> allQuestions = questionRepository.findAllByQuestionBank(questionBank);
        for (Question question : allQuestions) {
            question.getTags().clear(); // Clear join table 'question_tag' entries
        }

        // Explicitly flush to remove Join Table records before child entities are deleted.
        // This handles cases where ddl-auto=update didn't apply ON DELETE CASCADE to FKs.
        entityManager.flush();
        
        bankRepository.delete(questionBank);
    }

    @Transactional(readOnly = true)
    public Question getQuestionEntityById(String questionId) {
        Optional<Question> questionOptional = questionRepository.findById(UUID.fromString(questionId));
        if (questionOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        Question question = questionOptional.get();
        if (question.getQuestionBank().getAccess() == QuestionBank.Access.PUBLIC)
            return question;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole().equals("ROLE_ADMIN"))
            return question;
        QuestionBank bank = question.getQuestionBank();
        if (bank.getUser().getId().equals(user.getId()))
            return question;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this question bank");
    }

    private boolean canBeEdited(Question question) {
        QuestionBank questionBank = question.getQuestionBank();
        return canBeEdited(questionBank);
    }

    @Transactional
    public QuestionDTO saveQuestion(String bankId, QuestionCreationDTO questionCreationDTO) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        Question question = questionCreationDTO.toQuestion(questionBank);
        if (!canBeEdited(question))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to save this question");
        question = questionRepository.save(question);
        return new QuestionDTO(question);
    }

    @Transactional
    public List<QuestionDTO> saveAllQuestions(String bankId, List<QuestionCreationDTO> questionCreationDTOs) {
        QuestionBank questionBank = getQuestionBankEntityById(bankId);
        List<Question> questions = questionCreationDTOs.stream()
                .map(item -> item.toQuestion(questionBank))
                .toList();
        questions = questionRepository.saveAll(questions);
        return questions.stream().map(QuestionDTO::new).toList();
    }

    @Transactional
    public QuestionDTO updateQuestion(String questionId, QuestionUpdateDTO questionUpdateDTO) {
        Question question = getQuestionEntityById(questionId);
        if (!canBeEdited(question))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to update this question");
        questionUpdateDTO.updateQuestion(question);
        question = questionRepository.save(question);
        return new QuestionDTO(question);
    }

    @Transactional
    public List<QuestionDTO> overwriteQuestions(String bankId, List<QuestionCreationDTO> questionCreationDTOS) {
        QuestionBank bank = getQuestionBankEntityById(bankId);
        if (!canBeEdited(bank))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to overwrite questions in this bank");
        List<Question> questions = questionCreationDTOS.stream()
                .map(item -> item.toQuestion(bank))
                .toList();
        questions.forEach(question -> question.setQuestionBank(bank));

        bank.getQuestions().clear();
        bank.getQuestions().addAll(questions);

        QuestionBank savedBank = bankRepository.save(bank);
        return savedBank.getQuestions()
                .stream()
                .map(QuestionDTO::new)
                .toList();
    }

    @Transactional
    public void deleteQuestion(String questionId) {
        Question question = getQuestionEntityById(questionId);
        if (!canBeEdited(question))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this question");
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public QuestionComment getCommentEntityById(String commentId) {
        Optional<QuestionComment> commentOptional = questionCommentRepository.findById(UUID.fromString(commentId));
        if (commentOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        QuestionComment comment = commentOptional.get();
        getQuestionEntityById(comment.getQuestion().getId().toString());
        return comment;
    }

    @Transactional(readOnly = true)
    public List<QuestionCommentDTO> getComments(String questionId) {
        Question question = getQuestionEntityById(questionId);
        List<QuestionComment> comments = question.getComments();
        return comments.stream()
                .map(QuestionCommentDTO::new)
                .toList();
    }

    @Transactional
    public QuestionCommentDTO addComment(String questionId, QuestionCommentCreationDTO commentCreationDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Question question = getQuestionEntityById(questionId);
        QuestionComment comment = commentCreationDTO.toComment(user, question);
        comment = questionCommentRepository.save(comment);
        return new QuestionCommentDTO(comment);
    }

    @Transactional
    public QuestionCommentDTO updateComment(String commentId, QuestionCommentUpdateDTO commentUpdateDTO) {
        QuestionComment questionComment = getCommentEntityById(commentId);
        commentUpdateDTO.updateComment(questionComment);
        questionComment = questionCommentRepository.save(questionComment);
        return new QuestionCommentDTO(questionComment);
    }
}
