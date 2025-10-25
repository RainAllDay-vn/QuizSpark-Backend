package com.quizspark.quizsparkserver.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Value("${FIREBASE_JSON_PATH}")
    private String firebaseJsonPath;

    @PostConstruct
    public void init() {
        try {
            FileInputStream serviceAccount = new FileInputStream(firebaseJsonPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            // Log and continue
            System.out.println("Firebase JSON not found, skipping initialization for tests.");
        }
    }
}