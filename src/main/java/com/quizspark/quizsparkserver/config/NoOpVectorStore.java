package com.quizspark.quizsparkserver.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NoOpVectorStore implements VectorStore {
    @Override
    public void add(List<Document> documents) {
        // No-op
    }

    @Override
    public Optional<Boolean> delete(List<String> idList) {
        return Optional.of(true);
    }

    @Override
    public List<Document> similaritySearch(String query) {
        return Collections.emptyList();
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        return Collections.emptyList();
    }
}
