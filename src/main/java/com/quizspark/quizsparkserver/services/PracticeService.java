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

import java.util.*;

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

    public List<Question> getSuitableQuestions(QuestionBank bank, User user, int limit, List<String> tags){
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
    public UUID startNewPractice(String questionBankId, int limit, boolean shuffleAnswer, boolean revealAnswer, List<String> tags) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuestionBank bank = questionService.getQuestionBankById(questionBankId);
        List<Question> suitableQuestions = getSuitableQuestions(bank, user, limit, tags);
        Practice practice = new Practice(user, suitableQuestions, shuffleAnswer, revealAnswer);
        practice = practiceRepository.save(practice);
        return practice.getId();
    }

    public Practice startNewAnonymPractice(String questionBankId, int limit, boolean shuffleAnswer, List<String> tags) {
        Set<String> tagSet = new HashSet<>(tags);
        QuestionBank bank = questionService.getQuestionBankById(questionBankId);
        List<Question> questions = bank.getQuestions()
                .stream()
                .filter(question -> tagSet.containsAll(question.getTags().stream().map(Tag::getName).toList()))
                .limit(limit)
                .toList();
        return new Practice(null, questions, shuffleAnswer, true);
    }

    public List<Practice> getUserPractice(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return practiceRepository.findByUser(user);
    }

    public Practice getPractice(String practiceId){
        Optional<Practice> practiceOptional = practiceRepository.findById(UUID.fromString(practiceId));
        if (practiceOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There isn't a practice with such ID");
        Practice practice = practiceOptional.get();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!practice.getUser().equals(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this practice");
        practice.getPracticeQuestion().sort(Comparator.comparingInt(q -> q.getId().getIndex()));
        return practice;
    }

    public PracticeQuestion getPracticeQuestion(String practiceId, int index){
        Optional<PracticeQuestion> practiceQuestionOptional = practiceQuestionRepository.findByPracticeAndIndex(UUID.fromString(practiceId), index);
        if (practiceQuestionOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There isn't a practice question with such ID");
        PracticeQuestion practiceQuestion = practiceQuestionOptional.get();
        Practice practice = practiceQuestion.getPractice();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!practice.getUser().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this practice question");
        return practiceQuestion;
    }

    public List<String> answerPracticeQuestion(String practiceId, int index, List<String> userAnswer, int secondsToAnswer){
        PracticeQuestion question = getPracticeQuestion(practiceId, index);
        if (question.isClosed()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot answer this question");
        List<String> correctAnswer = question.answer(userAnswer, secondsToAnswer);
        practiceQuestionRepository.save(question);
        return correctAnswer;
    }
}