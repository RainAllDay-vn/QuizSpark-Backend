package com.quizspark.quizsparkserver.models;

import java.util.*;

public class Test {
    private final String[] questions;
    private final String[][] choices;
    private final int[] answers;
    private final int[] userAnswers;

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
            for (int j = 0; j < quizChoices.size(); j++) choices[i][answerMapping.get(j)] = quizChoices.get(j);
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

    public List<String> getQuestions() {
        return Arrays.asList(questions);
    }

    public List<List<String>> getChoices() {
        List<List<String>> result = new LinkedList<>();
        for (int i = 0; i < questions.length; i++) result.add(Arrays.asList(choices[i]));
        return result;
    }
}
