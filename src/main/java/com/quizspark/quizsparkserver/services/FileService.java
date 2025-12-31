package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.FileDTO;
import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.repositories.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final QuestionService questionService;

    @Autowired
    public FileService(FileRepository fileRepository, QuestionService questionService) {
        this.fileRepository = fileRepository;
        this.questionService = questionService;
    }

    public DbFile getFileEntity(String fileId) {
        return fileRepository.findById(UUID.fromString(fileId))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id " + fileId));
    }

    public FileDTO storeFile(String bankId, MultipartFile file) {
        QuestionBank bank = questionService.getQuestionBankEntityById(bankId);
        // Normalize file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sorry! Filename contains invalid path sequence " + fileName);
            }

            String contentType = determineContentType(fileName, file.getContentType());
            DbFile dbFile = new DbFile(fileName, contentType, file.getBytes());
            dbFile.setQuestionBank(bank);

            return new FileDTO(fileRepository.save(dbFile));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public void deleteFile(String bankId, String fileId) {
        DbFile file = getFileEntity(fileId);
        if (!file.getQuestionBank().getId().toString().equals(bankId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File does not belong to the specified question bank");
        }
        fileRepository.delete(file);
    }

    private String determineContentType(String fileName, String originalContentType) {
        String lowerCaseName = fileName.toLowerCase();
        if (lowerCaseName.endsWith(".md")) {
            return "text/markdown";
        } else if (lowerCaseName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerCaseName.endsWith(".json")) {
            return "application/json";
        } else if (lowerCaseName.endsWith(".pdf")) {
            return "application/pdf";
        }
        return originalContentType;
    }
}
