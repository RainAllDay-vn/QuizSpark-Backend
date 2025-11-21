package com.quizspark.quizsparkserver.models;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class PracticeStatistic{
    private static class QuestionAndScore{
        private final Question question;
        private double score;

        public QuestionAndScore(Question question, double score) {
            this.question = question;
            this.score = score;
        }
    }

    private Map<UUID, QuestionAndScore> scoreMap = new HashMap<>();

    public PracticeStatistic(List<Question> questions) {
        questions.forEach(q -> scoreMap.put(q.getId(), new QuestionAndScore(q, 0.0)));
    }

    public void calculateStatistic(List<PracticeQuestion> practiceHistory){
        for (PracticeQuestion question: practiceHistory){
            QuestionAndScore questionAndScore = scoreMap.get(question.getQuestion().getId());
            if (questionAndScore == null) continue;
            if (question.getIsCorrect() == null) questionAndScore.score-=0.4;
            else if (question.getIsCorrect()) questionAndScore.score+=1;
            else questionAndScore.score-=1.5;
        }
    }

    public List<Question> getResult(){
        return scoreMap.values().stream()
                .sorted(Comparator.comparingDouble(q -> q.score))
                .map(q -> q.question)
                .collect(Collectors.toList());
    }
}
