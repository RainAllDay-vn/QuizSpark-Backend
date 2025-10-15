package com.quizspark.quizsparkserver.config;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.Quiz;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.CollectionRepository;
import com.quizspark.quizsparkserver.repositories.QuizRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("test")
public class TestDataConfig {

    @Bean
    CommandLineRunner testDataInitializer(
            UserRepository userRepository,
            CollectionRepository collectionRepository,
            QuizRepository quizRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Clear existing data
            quizRepository.deleteAll();
            collectionRepository.deleteAll();
            userRepository.deleteAll();

            // --- Create 3 Users ---
            List<User> users = new ArrayList<>();
            users.add(new User("bob", "bob", passwordEncoder));
            users.add(new User("alice", "alice", passwordEncoder));
            users.add(new User("charlie", "charlie", passwordEncoder));
            users.get(0).setRole("ROLE_ADMIN");

            userRepository.saveAll(users);

            List<Collection> allCollections = new ArrayList<>();
            List<Quiz> allQuizzes = new ArrayList<>();

            int collectionsPerUser = 3;
            int quizzesPerCollection = 8;

            // --- Create Collections & Quizzes ---
            for (User user : users) {
                for (int i = 1; i <= collectionsPerUser; i++) {
                    Collection collection = new Collection();
                    collection.setName(user.getUsername() + "'s Collection " + i);
                    collection.setDescription("A quiz collection created by " + user.getUsername());
                    collection.setAccess(i%2==0 ? Collection.Access.PUBLIC : Collection.Access.PRIVATE); // Alternate between public/private
                    collection.setUser(user);

                    allCollections.add(collection);

                    // --- Create Quizzes ---
                    for (int j = 1; j <= quizzesPerCollection; j++) {
                        Quiz quiz = new Quiz();
                        quiz.setCollection(collection);
                        quiz.setQuestion("Question " + j + " from " + collection.getName());
                        quiz.setAnswer("Answer " + j);
                        quiz.setChoice1("Choice A" + j);
                        quiz.setChoice2("Choice B" + j);
                        quiz.setChoice3("Choice C" + j);
                        quiz.setChoice4("Choice D" + j);

                        allQuizzes.add(quiz);
                    }
                }
            }

            collectionRepository.saveAll(allCollections);
            quizRepository.saveAll(allQuizzes);

            System.out.println("✅ Test data inserted into H2");
            System.out.printf("Users: %d, Collections: %d, Quizzes: %d%n",
                    userRepository.count(), collectionRepository.count(), quizRepository.count());
        };
    }
}
