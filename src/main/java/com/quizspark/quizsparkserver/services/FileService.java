package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.FileDTO;
import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final QuestionService questionService;

    public FileService(FileRepository fileRepository, QuestionService questionService) {
        this.fileRepository = fileRepository;
        this.questionService = questionService;
    }

    public DbFile getFileEntity(String fileId) {
        return fileRepository.findById(UUID.fromString(fileId))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id " + fileId));
    }

    public DbFile getFileEntityByName(String fileName) {
        return fileRepository.findByFileName(fileName)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "File not found with name " + fileName));
    }

    public DbFile getParseFileEntity(DbFile originalFile) {
        String parseFileName = originalFile.getFileName() + "_PARSE";
        DbFile parseFile = getFileEntityByName(parseFileName);
        return parseFile;
    }

    public FileDTO storeFile(MultipartFile file) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Normalize file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sorry! Filename contains invalid path sequence " + fileName);
            }

            String contentType = determineContentType(fileName, file.getContentType());
            DbFile dbFile = new DbFile(fileName, contentType, file.getBytes(), user);

            return new FileDTO(fileRepository.save(dbFile));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public FileDTO assignFileToBank(String fileId, String bankId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DbFile file = getFileEntity(fileId);
        if (!file.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this file");
        }
        QuestionBank bank = questionService.getQuestionBankEntityById(bankId);

        // Remove from current bank if any to maintain consistency
        if (file.getQuestionBank() != null) {
            file.getQuestionBank().getFiles().remove(file);
        }

        file.setQuestionBank(bank);
        bank.getFiles().add(file);

        return new FileDTO(fileRepository.save(file));
    }

    public void unassignFile(String fileId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DbFile file = getFileEntity(fileId);
        if (!file.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this file");
        }

        if (file.getQuestionBank() != null) {
            file.getQuestionBank().getFiles().remove(file);
            file.setQuestionBank(null);
            fileRepository.save(file);
        }
    }

    public List<FileDTO> getUserFiles() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fileRepository.findByUser(user).stream()
                .map(FileDTO::new)
                .toList();
    }

    public void deleteFile(String fileId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DbFile file = getFileEntity(fileId);
        if (!file.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this file");
        }
        if (file.getQuestionBank() != null) {
            file.getQuestionBank().getFiles().remove(file);
        }
        user.getFiles().remove(file);
        file.setQuestionBank(null);
        file.setUser(null);

        fileRepository.delete(file);
    }

    public DbFile createMirrorFile(DbFile source, String content) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DbFile mirrorFile = new DbFile(
                source.getFileName() + ".txt",
                "text/plain",
                content.getBytes(),
                user);
        mirrorFile.setQuestionBank(source.getQuestionBank());
        mirrorFile.setOriginFile(source);
        return fileRepository.save(mirrorFile);
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
