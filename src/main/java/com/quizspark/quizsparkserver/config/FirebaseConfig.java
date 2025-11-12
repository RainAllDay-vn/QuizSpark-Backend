package com.quizspark.quizsparkserver.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Log4j2
public class FirebaseConfig {
    @Value("${FIREBASE_JSON_PATH}")
    private String firebaseJsonPath;

    private static final String DEFAULT_CLASSPATH_RESOURCE = "backend-private-key.json";

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase App already initialized.");
            return;
        }

        try {
            FirebaseOptions options = getFirebaseOptions();
            FirebaseApp.initializeApp(options);
            log.info("Firebase application has been initialized successfully.");

        } catch (IOException e) {
            log.error("FATAL: Firebase initialization failed. Please check your configuration.", e);
        }
    }

    private FirebaseOptions getFirebaseOptions() throws IOException {
        // Priority 1: Use the path from the environment variable (injected by @Value)
        if (firebaseJsonPath != null && !firebaseJsonPath.trim().isEmpty()) {
            log.info("Initializing Firebase using file from path: {}", firebaseJsonPath);
            try (InputStream serviceAccount = new FileInputStream(firebaseJsonPath)) {
                return FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
            }
        }

        // Priority 2: Fallback to classpath resource for local development
        log.info("Firebase path not configured. Attempting to load from classpath resource: {}", DEFAULT_CLASSPATH_RESOURCE);
        ClassPathResource resource = new ClassPathResource(DEFAULT_CLASSPATH_RESOURCE);
        try (InputStream serviceAccount = resource.getInputStream()) {
            return FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        }
    }
}