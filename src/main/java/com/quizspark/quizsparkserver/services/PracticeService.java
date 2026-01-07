package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.practice.PracticeAnswerResponseDTO;
import com.quizspark.quizsparkserver.dto.practice.PracticeDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import com.quizspark.quizsparkserver.models.PracticeStatistic;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.PracticeQuestionRepository;
import com.quizspark.quizsparkserver.repositories.PracticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class PracticeService {
    private final QuestionService questionService;
    private final PracticeRepository practiceRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;

    public PracticeService(
            QuestionService questionService,
            PracticeRepository practiceRepository,
            PracticeQuestionRepository practiceQuestionRepository) {
        this.questionService = questionService;
        this.practiceRepository = practiceRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
    }

    private List<Question> getSuitableQuestions(QuestionBank bank, User user, int limit, List<String> tags) {
        Set<String> tagSet = new HashSet<>(tags);
        List<Question> questions = bank.getQuestions()
                .stream()
                .filter(question -> tagSet.containsAll(question.getTags().stream().map(Tag::getName).toList()))
                .toList();
        List<PracticeQuestion> practiceHistory = practiceQuestionRepository.findByQuestionsAndUser(questions, user);
        PracticeStatistic practiceStatistic = new PracticeStatistic(questions);
        practiceStatistic.calculateStatistic(practiceHistory);
        return practiceStatistic.getResult()
                .stream()
                .limit(limit)
                .toList();
    }

    @Transactional
    public UUID startNewPractice(String questionBankId, int limit, boolean shuffleAnswer, boolean revealAnswer,
            List<String> tags) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questionService.getQuestionBankEntityById(questionBankId);
        List<Question> suitableQuestions = new ArrayList<>(getSuitableQuestions(bank, user, limit, tags));
        if (suitableQuestions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found matching the criteria");
        }
        Collections.shuffle(suitableQuestions);
        Practice practice = new Practice(user, suitableQuestions, shuffleAnswer, revealAnswer);
        practice = practiceRepository.save(practice);
        return practice.getId();
    }

    @Transactional
    public PracticeDTO startNewAnonymPractice(String questionBankId, int limit, boolean shuffleAnswer,
            List<String> tags) {
        Set<String> tagSet = new HashSet<>(tags);
        QuestionBank bank = questionService.getQuestionBankEntityById(questionBankId);
        List<Question> questions = bank.getQuestions()
                .stream()
                .filter(question -> tagSet.containsAll(question.getTags().stream().map(Tag::getName).toList()))
                .limit(limit)
                .toList();
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found matching the criteria");
        }
        Practice practice = new Practice(null, questions, shuffleAnswer, true);
        return new PracticeDTO(practice);
    }

    @Transactional(readOnly = true)
    public Page<PracticeDTO> getUserPractice(Pageable pageable, Boolean isClosed) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Practice> page = practiceRepository.findByUser(user, isClosed, pageable);
        return page.map(PracticeDTO::new);
    }

    @Transactional(readOnly = true)
    public PracticeDTO getPractice(String practiceId) {
        return new PracticeDTO(getPracticeEntity(practiceId));
    }

    private Practice getPracticeEntity(String practiceId) {
        Optional<Practice> practiceOptional = practiceRepository.findById(UUID.fromString(practiceId));
        if (practiceOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There isn't a practice with such ID");
        Practice practice = practiceOptional.get();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!practice.getUser().equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this practice");
        practice.getPracticeQuestion().sort(Comparator.comparingInt(q -> q.getId().getIndex()));
        return practice;
    }

    private PracticeQuestion getPracticeQuestionEntity(String practiceId, int index) {
        Optional<PracticeQuestion> practiceQuestionOptional = practiceQuestionRepository
                .findByPracticeAndIndex(UUID.fromString(practiceId), index);
        if (practiceQuestionOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There isn't a practice question with such ID");
        PracticeQuestion practiceQuestion = practiceQuestionOptional.get();
        Practice practice = practiceQuestion.getPractice();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!practice.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this practice question");
        return practiceQuestion;
    }

    @Transactional
    public PracticeAnswerResponseDTO answerPracticeQuestion(String practiceId, int index, List<String> userAnswer,
            int secondsToAnswer) {
        PracticeQuestion question = getPracticeQuestionEntity(practiceId, index);
        if (question.isClosed())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot answer this question");
        question.updateUserAnswer(userAnswer, secondsToAnswer);
        practiceQuestionRepository.save(question);
        PracticeAnswerResponseDTO dto = new PracticeAnswerResponseDTO();
        if (question.getPractice().isRevealAnswer())
            dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setQuestionComments(question.getQuestion().getComments().stream().map(QuestionCommentDTO::new).toList());
        return dto;
    }

    @Transactional
    public PracticeDTO finishPractice(String practiceId) {
        Practice practice = getPracticeEntity(practiceId);
        practice.setClosed(true);
        practice = practiceRepository.save(practice);
        return new PracticeDTO(practice);
    }
}
