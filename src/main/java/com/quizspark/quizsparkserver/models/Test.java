package com.quizspark.quizsparkserver.models;

import java.util.*;

public class Test {
    private final String[] questions;
    private final String[][] choices;
    private final int[] answers;
    private final int[] userAnswers;
    private final Date startDate;
    private final Date endDate;

    public Test(QuestionBank questionBank) {
        List<Question> questions = questionBank.getQuestions();
        this.questions = new String[questions.size()];
        this.choices = new String[questions.size()][];
        this.answers = new int[questions.size()];
        this.userAnswers = new int[questions.size()];
        this.startDate = new Date();
        this.endDate = null;
        List<Integer> answerMapping = new ArrayList<>(5);

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            List<String> quizChoices = question.getChoices();
            choices[i] = new String[quizChoices.size()];
            answerMapping.clear();
            for (int j = 0; j < quizChoices.size(); j++) answerMapping.add(j);
            Collections.shuffle(answerMapping);

            this.questions[i] = question.getDescription();
            for (int j = 0; j < quizChoices.size(); j++) choices[i][answerMapping.get(j)] = quizChoices.get(j);
            answers[i] = answerMapping.get(question.getAnswer());
        }
    }

    public int submitAnswer(int questionNumber, int answer) {
        if (endDate != null && endDate.after(new Date())) {
            userAnswers[questionNumber] = answer;
            return answers[questionNumber];
        }
        else return -1;
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
