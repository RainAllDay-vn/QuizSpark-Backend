package com.quizspark.quizsparkserver.dto.questions;

import com.quizspark.quizsparkserver.models.Question;

import java.util.Collections;
import java.util.List;

public class ChoicesAndAnswerProcessor {
    public static void processSingleAnswerQuestion(Question question, List<String> choices, List<String> answer){
        if (choices==null || choices.size()<2) throw new IllegalArgumentException("Single answer question must have at least 2 choices");
        if (answer==null || answer.size()!=1) throw new IllegalArgumentException("Single answer question must have exactly 1 answer");
        int answerInt = Integer.parseInt(answer.get(0));
        if (answerInt >= choices.size() || answerInt < 0) throw new IllegalArgumentException("Answer index out of range");
        question.setChoices(choices);
        question.setAnswer(answer);
    }

    public static void processMultipleAnswerQuestion(Question question, List<String> choices, List<String> answer){
        if (choices==null || choices.size()<2) throw new IllegalArgumentException("Multiple answer question must have at least 2 choices");
        if (answer==null || answer.isEmpty()) throw new IllegalArgumentException("Multiple answer question must have at least 1 answer");
        for (String ans : answer){
            int answerInt = Integer.parseInt(ans);
            if (answerInt >= choices.size() || answerInt < 0) throw new IllegalArgumentException("Answer index out of range");
        }
        question.setChoices(choices);
        Collections.sort(answer);
        question.setAnswer(answer);
    }

    public static void processFillTheBlankQuestion(Question question, List<String> choices, List<String> answer){
        if (choices==null || choices.isEmpty()) throw new IllegalArgumentException("Fill the blank question must have at least 1 choice");
        if (answer==null || answer.size()!=choices.size()) throw new IllegalArgumentException("Fill the blank question must have same number of choices and answers");
        question.setChoices(choices);
        question.setAnswer(answer);
    }

    public static void processOpenAnswerQuestion(Question question){
        question.setChoices(null);
        question.setAnswer(null);
    }
}
