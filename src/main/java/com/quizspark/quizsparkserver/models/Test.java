package com.quizspark.quizsparkserver.models;

import java.util.*;

public class Test {
    private String[] questions;
    private String[][] choices;
    private int[] answers;
    private int[] userAnswers;

    public Test(Collection collection) {
        List<Quiz> quizzes = collection.getQuizzes();
        this.questions = new String[quizzes.size()];
        this.choices = new String[quizzes.size()][];
        this.answers = new int[quizzes.size()];
        this.userAnswers = new int[quizzes.size()];
        List<Integer> answerMapping = new ArrayList<>(5);

        for (int i = 0; i < quizzes.size(); i++) {
            Quiz quiz = quizzes.get(i);
            List<String> quizChoices = quiz.getChoices();
            choices[i] = new String[quizChoices.size()];
            answerMapping.clear();
            for (int j = 0; j < quizChoices.size(); j++) answerMapping.add(j);
            Collections.shuffle(answerMapping);

            questions[i] = quiz.getQuestion();
            for (int j = 0; j < quizChoices.size(); j++) choices[i][j] = quizChoices.get(answerMapping.get(j));
            answers[i] = answerMapping.get(quiz.getAnswer());
        }
    }

    public int submitAnswer(int questionNumber, int answer) {
        userAnswers[questionNumber] = answer;
        return answers[questionNumber];
    }

    public int getSize() {
        return questions.length;
    }

    public String getQuestion(int questionNumber) {
        return questions[questionNumber];
    }

    public List<String> getChoices(int questionNumber) {
        return Arrays.asList(choices[questionNumber]);
    }
}
