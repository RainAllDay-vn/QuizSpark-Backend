package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.practice.QuestionStatisticDTO;
import com.quizspark.quizsparkserver.models.*;
import com.quizspark.quizsparkserver.repositories.PracticeQuestionRepository;
import com.quizspark.quizsparkserver.repositories.PracticeRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public Practice getNewPractice(String questionBankId, int limit, boolean shuffleAnswer) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questionService.getQuestionBankById(questionBankId);
        List<Question> suitableQuestions = getSuitableQuestions(bank, user, limit);
        Practice practice = new Practice(user, bank, suitableQuestions, shuffleAnswer);
        practiceRepository.save(practice);
        return practice;
    }
}