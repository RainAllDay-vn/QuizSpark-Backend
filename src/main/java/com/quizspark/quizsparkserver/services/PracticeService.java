package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.models.PracticeStatistic;
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
        List<Question> questions = bank.getQuestions();
        List<PracticeQuestion> practiceHistory = practiceQuestionRepository.findByQuestionBankAndUser(bank, user);
        PracticeStatistic practiceStatistic = new PracticeStatistic(questions);
        practiceStatistic.calculateStatistic(practiceHistory);
        return practiceStatistic.getResult()
                .stream()
                .limit(limit)
                .toList();
    }

    @Transactional
    public UUID startNewPractice(String questionBankId, int limit, boolean shuffleAnswer, boolean revealAnswer) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questionService.getQuestionBankById(questionBankId);
        List<Question> suitableQuestions = getSuitableQuestions(bank, user, limit);
        Practice practice = new Practice(user, suitableQuestions, shuffleAnswer, revealAnswer);
        practice = practiceRepository.save(practice);
        return practice.getId();
    }

    public Practice startNewAnonymPractice(String questionBankId, int limit, boolean shuffleAnswer) {
        QuestionBank bank = questionService.getQuestionBankById(questionBankId);
        List<Question> questions = bank.getQuestions().stream().limit(limit).toList();
        return new Practice(null, questions, shuffleAnswer, true);
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