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

import org.springframework.transaction.annotation.Transactional;
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
        DbFile file = fileRepository.findById(UUID.fromString(fileId))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id " + fileId));
        verifyOwnership(file);
        return file;
    }

    public DbFile getFileEntityByName(String fileName) {
        DbFile file = fileRepository.findByFileName(fileName)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "File not found with name " + fileName));
        verifyOwnership(file);
        return file;
    }

    public DbFile getParseFileEntity(DbFile originalFile) {
        String parseFileName = originalFile.getFileName() + "_PARSE";
        DbFile parseFile = getFileEntityByName(parseFileName);
        verifyOwnership(parseFile);
        return parseFile;
    }

    private void verifyOwnership(DbFile file) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!file.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this file");
        }
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

    @Transactional
    public FileDTO assignFileToBank(String fileId, String bankId) {
        DbFile file = getFileEntity(fileId);
        QuestionBank bank = questionService.getQuestionBankEntityById(bankId);

        if (!bank.getFiles().contains(file)) {
            bank.getFiles().add(file);
            file.getQuestionBanks().add(bank);
        }

        return new FileDTO(fileRepository.save(file));
    }

    @Transactional
    public void unassignFile(String fileId, String bankId) {
        DbFile file = getFileEntity(fileId);
        QuestionBank bank = questionService.getQuestionBankEntityById(bankId);

        bank.getFiles().remove(file);
        file.getQuestionBanks().remove(bank);
        fileRepository.save(file);
    }

    public List<FileDTO> getUserFiles() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fileRepository.findByUserId(user.getId()).stream()
                .map(FileDTO::new)
                .toList();
    }

    @Transactional
    public void deleteFile(String fileId) {
        DbFile file = getFileEntity(fileId);
        verifyOwnership(file);
        for (QuestionBank bank : file.getQuestionBanks()) {
            bank.getFiles().remove(file);
        }
        file.getQuestionBanks().clear();
        fileRepository.delete(file);
    }

    public DbFile createMirrorFile(DbFile source, String content) {
        DbFile mirrorFile = new DbFile(
                source.getFileName() + ".txt",
                "text/plain",
                content.getBytes(),
                source.getUser());
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
