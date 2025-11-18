package com.quizspark.quizsparkserver.config;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Question;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.QuestionBankRepository;
import com.quizspark.quizsparkserver.repositories.QuestionRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@Profile("test")
public class TestDataConfig {

    @Bean
    CommandLineRunner testDataInitializer(
            UserRepository userRepository,
            QuestionBankRepository questionBankRepository,
            QuestionRepository questionRepository
    ) {
        return args -> {
            // Clear existing data
            questionRepository.deleteAll();
            questionBankRepository.deleteAll();
            userRepository.deleteAll();

            // --- Create 3 Users ---
            List<User> users = new ArrayList<>();
            users.add(new User("bob", "lnvlong.dev@gmail.com"));
            users.add(new User("alice", "alice@mail.com"));
            users.add(new User("charlie", "charlie@mail.com"));
            users.get(0).setRole("ROLE_ADMIN");

            userRepository.saveAll(users);

            List<QuestionBank> allQuestionBanks = new ArrayList<>();
            List<Question> allQuizzes = new ArrayList<>();

            int collectionsPerUser = 3;
            int quizzesPerCollection = 8;

            // --- Create Collections & Quizzes ---
            for (User user : users) {
                for (int i = 1; i <= collectionsPerUser; i++) {
                    QuestionBank questionBank = new QuestionBank();
                    questionBank.setName(user.getUsername() + "'s Collection " + i);
                    questionBank.setDescription("A quiz collection created by " + user.getUsername());
                    questionBank.setStatus(QuestionBank.Status.PUBLISHED);
                    questionBank.setAccess(i%2==0 ? QuestionBank.Access.PUBLIC : QuestionBank.Access.PRIVATE); // Alternate between public/private
                    questionBank.setUser(user);
                    questionBank.setCreatedAt(new Date());
                    questionBank.setNumberOfAttempts(0);
                    questionBank.setRating(5.0f);

                    allQuestionBanks.add(questionBank);

                    // --- Create Quizzes ---
                    for (int j = 1; j <= quizzesPerCollection; j++) {
                        Question question = new Question();
                        question.setQuestionBank(questionBank);
                        question.setDescription("Question " + j + " from " + questionBank.getName());
                        question.setAnswer(0);
                        List<String> choices = new ArrayList<>();
                        for (char c = 'A'; c <= 'D'; c++) choices.add("Choice " + c + j);
                        question.setChoices(choices);

                        allQuizzes.add(question);
                    }
                }
            }

            questionBankRepository.saveAll(allQuestionBanks);
            questionRepository.saveAll(allQuizzes);

            System.out.println("✅ Test data inserted into H2");
            System.out.printf("Users: %d, Collections: %d, Quizzes: %d%n",
                    userRepository.count(), questionBankRepository.count(), questionRepository.count());
        };
    }
}
