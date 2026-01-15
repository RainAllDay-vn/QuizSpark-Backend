package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.practice.PracticeAnswerResponseDTO;
import com.quizspark.quizsparkserver.dto.practice.PracticeDTO;
import com.quizspark.quizsparkserver.dto.practice.PracticeQuestionDTO;
import com.quizspark.quizsparkserver.dto.questions.QuestionCommentDTO;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
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
import java.util.stream.Collectors;

@Service
public class PracticeService {
    private final QuestionService questionService;
    private final PracticeRepository practiceRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final AiService aiService;

    public PracticeService(
            QuestionService questionService,
            PracticeRepository practiceRepository,
            PracticeQuestionRepository practiceQuestionRepository,
            AiService aiService) {
        this.questionService = questionService;
        this.practiceRepository = practiceRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.aiService = aiService;
    }

    private List<Question> sortByRelevantScore(List<Question> questions) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<PracticeQuestion> practiceHistory = practiceQuestionRepository.findByQuestionBankAndUser(questions, user);
        PracticeStatistic practiceStatistic = new PracticeStatistic(questions);
        practiceStatistic.calculateStatistic(practiceHistory);
        return practiceStatistic.getResult();
    }

    @Transactional
    public UUID startNewPractice(String questionBankId, int limit, boolean shuffleAnswer, boolean revealAnswer,
                                 List<String> tags, boolean endlessMode) {
        QuestionBank bank = questionService.getQuestionBankEntityById(questionBankId);
        List<Question> questions = bank.getQuestions();
        return startNewPractice(bank, questions, limit, shuffleAnswer, revealAnswer, tags, endlessMode);
    }

    public UUID startNewPractice(QuestionBank bank, List<Question> questions, int limit, boolean shuffleAnswer, boolean revealAnswer,
                                 List<String> tags, boolean endlessMode) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<String> tagSet = new HashSet<>(tags);
        questions = questions.stream()
                .filter(question -> tagSet.containsAll(question.getTags().stream().map(Tag::getName).toList()))
                .toList();
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found matching the criteria");
        }
        questions = sortByRelevantScore(questions);
        questions = questions.subList(0, Math.min(questions.size(), limit));
        Collections.shuffle(questions);
        Practice practice = new Practice(user, bank, questions, shuffleAnswer, revealAnswer, endlessMode);
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
        Practice practice = new Practice(null, bank, questions, shuffleAnswer, true);
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
            dto.setCorrectAnswer(question.getAnswer());
            
        if (question.getBaseQuestion() != null) {
            dto.setQuestionComments(question.getBaseQuestion()
                    .getComments()
                    .stream()
                    .map(QuestionCommentDTO::new)
                    .toList());
        } else {
            dto.setQuestionComments(Collections.emptyList());
        }
        return dto;
    }

    @Transactional
    public PracticeDTO finishPractice(String practiceId) {
        Practice practice = getPracticeEntity(practiceId);
        practice.setClosed(true);
        practice = practiceRepository.save(practice);
        return new PracticeDTO(practice);
    }

    @Transactional
    public UUID duplicatePractice(String practiceId) {
        Practice practice = getPracticeEntity(practiceId);
        List<PracticeQuestion> practiceQuestions = practice.getPracticeQuestion();
        if (practiceQuestions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot duplicate a practice with no questions");
        }

        QuestionBank bank = practice.getQuestionBank();
        if (bank == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Practice is not associated with a question bank");
        int size = practiceQuestions.size();
        boolean revealAnswer = practice.isRevealAnswer();
        boolean shuffleAnswer = true;
        boolean endlessMode = practice.isEndlessMode();
        List<String> tags = practiceQuestions.stream()
                .flatMap(pq -> pq.getTags().stream())
                .map(TagDTO::getName)
                .distinct()
                .toList();
        List<Question> questions = bank.getQuestions();

        return startNewPractice(bank, questions, size, shuffleAnswer, revealAnswer, tags, endlessMode);
    }

    @Transactional
    public List<PracticeQuestionDTO> fetchNextQuestionsManual(String practiceId, int numberOfQuestions, int aiQuestions) {
        Practice practice = getPracticeEntity(practiceId);
        if (!practice.isEndlessMode()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This practice is not in endless mode");
        }

        List<PracticeQuestion> currentQuestions = practice.getPracticeQuestion();
        Set<UUID> usedQuestionIds = currentQuestions.stream()
                .map(pq -> pq.getBaseQuestion() != null ? pq.getBaseQuestion().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Question> questionsToAdd = new ArrayList<>();
        int questionsFromDbNeeded = numberOfQuestions - aiQuestions;

        if (questionsFromDbNeeded > 0) {
            QuestionBank bank = practice.getQuestionBank();
            List<Question> unseenQuestions = bank.getQuestions().stream()
                    .filter(q -> !usedQuestionIds.contains(q.getId()))
                    .limit(questionsFromDbNeeded)
                    .toList();
            questionsToAdd.addAll(unseenQuestions);
        }

        int shortfall = Math.max(0, questionsFromDbNeeded - (questionsToAdd.size()));
        int totalAiNeeded = aiQuestions + shortfall;

        if (totalAiNeeded > 0) {
            AiService.AdaptiveGenerationResponse aiResponse = aiService.generateAdaptiveQuestions(practice, totalAiNeeded);
            practice.setAiScratchPad(aiResponse.getScratchPad());
            
            QuestionBank bank = practice.getQuestionBank();
            List<Question> generatedQuestions = questionService.saveAllQuestions(bank.getId().toString(), aiResponse.getQuestions())
                    .stream()
                    .map(dto -> questionService.getQuestionEntityById(dto.getId().toString()))
                    .toList();
            questionsToAdd.addAll(generatedQuestions);
        }

        int nextIndex = currentQuestions.stream()
                .mapToInt(pq -> pq.getId().getIndex())
                .max()
                .orElse(-1) + 1;

        List<PracticeQuestion> newPracticeQuestions = new ArrayList<>();
        for (Question q : questionsToAdd) {
            PracticeQuestion pq = new PracticeQuestion(practice, nextIndex++, q, true);
            newPracticeQuestions.add(pq);
        }

        practice.getPracticeQuestion().addAll(newPracticeQuestions);
        practiceRepository.save(practice);

        return newPracticeQuestions.stream().map(PracticeQuestionDTO::new).toList();
    }

    @Transactional
    public void discardQuestion(String practiceId, int index) {
        // This ensures ownership and existence
        PracticeQuestion pq = getPracticeQuestionEntity(practiceId, index);
        // We delete it from the practice's list as well to ensure session consistency if it was cached
        Practice practice = pq.getPractice();
        practice.getPracticeQuestion().remove(pq);
        practiceQuestionRepository.delete(pq);
    }
}
