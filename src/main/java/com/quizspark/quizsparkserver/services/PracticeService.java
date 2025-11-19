package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.practice.QuestionStatisticDTO;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.PracticeQuestionRepository;
import com.quizspark.quizsparkserver.repositories.PracticeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PracticeService {
    private final QuestionService questionService;
    private final PracticeRepository practiceRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;

    public PracticeService(
            QuestionService questionService,
            PracticeRepository practiceRepository,
            PracticeQuestionRepository practiceQuestionRepository
    ) {
        this.questionService = questionService;
        this.practiceRepository = practiceRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
    }

    public List<Question> getSuitableQuestions(QuestionBank bank, User user, int limit){
        List<PracticeQuestion> practiceHistory = practiceQuestionRepository.findByQuestionBankAndUser(bank, user);
        return practiceHistory.stream()
                .collect(Collectors.groupingBy(PracticeQuestion::getQuestion))
                .entrySet()
                .stream()
                .map(entry -> new QuestionStatisticDTO(entry.getKey(), entry.getValue()))
                .sorted()
                .map(QuestionStatisticDTO::getQuestion)
                .limit(limit)
                .toList();
    }

    @Transactional
    public UUID startNewPractice(String questionBankId, int limit, boolean shuffleAnswer, boolean revealAnswer) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questionService.getQuestionBankById(questionBankId);
        List<Question> suitableQuestions = getSuitableQuestions(bank, user, limit);
        Practice practice = new Practice(user, bank, suitableQuestions, shuffleAnswer, revealAnswer);
        practice = practiceRepository.save(practice);
        return practice.getId();
    }

    public Practice getPractice(String practiceId){
        Optional<Practice> practiceOptional = practiceRepository.findById(UUID.fromString(practiceId));
        if (practiceOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There isn't a practice with such ID");
        Practice practice = practiceOptional.get();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (practice.getUser().equals(user)) return practice;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this practice");
    }
}