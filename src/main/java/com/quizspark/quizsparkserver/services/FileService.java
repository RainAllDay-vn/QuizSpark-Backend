package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.FileDTO;
import com.quizspark.quizsparkserver.models.DbFile;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.FileRepository;

import io.qdrant.client.ConditionFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointsSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FileService {
    
    @Value("${spring.ai.vectorstore.qdrant.collection-name:vector_store}")
    private String collectionName;
    private final FileRepository fileRepository;
    private final QuestionService questionService;
    private final QdrantClient qdrantClient;

    public FileService(FileRepository fileRepository, QuestionService questionService, QdrantClient qdrantClient) {
        this.fileRepository = fileRepository;
        this.questionService = questionService;
        this.qdrantClient = qdrantClient;
    }

    @Transactional(readOnly = true)
    public DbFile getFileEntity(String fileId) {
        DbFile file = fileRepository.findById(UUID.fromString(fileId))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id " + fileId));
        if (!canView(file))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this file");
        return file;
    }

    @Transactional(readOnly = true)
    public DbFile getFileEntityWithContent(String fileId) {
        DbFile file = fileRepository.findByIdWithContent(UUID.fromString(fileId))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id " + fileId));
        if (!canView(file))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this file");
        return file;
    }

    @Transactional(readOnly = true)
    public Optional<DbFile> getDerivedFile(DbFile originalFile, String fileName) {
        List<DbFile> derivedFiles = fileRepository.findByOriginFileAndFileName(originalFile, fileName);
        if (derivedFiles.isEmpty())
            return Optional.empty();
        DbFile derivedFile = derivedFiles.get(0);
        return Optional.of(derivedFile);
    }

    @Transactional(readOnly = true)
    public Optional<DbFile> getDerivedFileWithContent(DbFile originalFile, String fileName) {
        List<DbFile> derivedFiles = fileRepository.findByOriginFileAndFileName(originalFile, fileName);
        if (derivedFiles.isEmpty())
            return Optional.empty();
        DbFile derivedFile = derivedFiles.get(0);
        // Trigger lazy loading
        if (derivedFile.getData() != null) {
            // just accessing to ensure loaded
        }
        return Optional.of(derivedFile);
    }

    private boolean canView(DbFile file) {
        if (file.getUser() == null)
            return true;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (file.getUser().getId().equals(user.getId()))
            return true;
        return false;
    }

    private boolean canEdit(DbFile file) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (file.getUser().getId().equals(user.getId()))
            return true;
        return false;
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public List<FileDTO> getUserFiles() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fileRepository.findByUserIdAndOriginFileIsNull(user.getId()).stream()
                .map(FileDTO::new)
                .toList();
    }

    @Transactional
    public void deleteFile(String fileId) {
        DbFile file = getFileEntity(fileId);
        if (!canEdit(file))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this file");
        for (QuestionBank bank : file.getQuestionBanks()) {
            bank.getFiles().remove(file);
        }
        file.getQuestionBanks().clear();
        
        try {
            // 1. Create a filter that matches the "fileId" metadata field
            Filter filter = Filter.newBuilder()
                .addMust(ConditionFactory.matchKeyword("fileId", fileId)) 
                .build();

            // 2. Create the PointsSelector
            PointsSelector selector = PointsSelector.newBuilder()
                .setFilter(filter)
                .build();

            // 3. Create the Delete Request
            DeletePoints deleteRequest = DeletePoints.newBuilder()
                .setCollectionName(collectionName)
                .setPoints(selector)
                .build();

            // 4. Execute the deletion and wait for it to complete (.get())
            qdrantClient.deleteAsync(deleteRequest).get();
        } catch (Exception e) {
            log.error("Failed to delete embeddings for file {}: {}", fileId, e.getMessage());
        }

        fileRepository.delete(file);
    }

    @Transactional
    public FileDTO createNoteFile(String fileId) {
        DbFile originalFile = getFileEntityWithContent(fileId);
        if (!"application/pdf".equals(originalFile.getFileType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Derived files can only be created from PDFs");
        }
        String content = "";
        try (PDDocument document = PDDocument.load(originalFile.getData())) {
            int pageCount = document.getNumberOfPages();
            if (pageCount > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pageCount - 1; i++) {
                    if (i > 0) sb.append("\n\n");
                    sb.append("---");
                }
                content = sb.toString();
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading PDF file", e);
        }
        DbFile derivedFile = createOrUpdateDerivedFile(originalFile, "NOTE.md", content);
        return new FileDTO(derivedFile);
    }

    @Transactional
    public DbFile createOrUpdateDerivedFile(DbFile baseFileRef, String fileName, String content) {
        // Reload baseFile to ensure it is attached to the current session
        DbFile baseFile = fileRepository.findById(baseFileRef.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Original file not found"));

        String fileType = "application/octet-stream";
        if (fileName.endsWith(".md")) {
            fileType = "text/markdown";
        }

        Optional<DbFile> existing = getDerivedFile(baseFile, fileName);
        if (existing.isPresent()) {
            DbFile derivedFile = existing.get();
            derivedFile.setFileType(fileType);
            derivedFile.setData(content.getBytes());
            return fileRepository.save(derivedFile);
        }

        DbFile derivedFile = new DbFile(fileName, fileType, content.getBytes(), baseFile.getUser());
        derivedFile.setOriginFile(baseFile);
        return fileRepository.save(derivedFile);
    }

    @Transactional
    public FileDTO updateFileContent(String fileId, String content) {
        DbFile file = getFileEntity(fileId);
        if (!canEdit(file))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this file");

        if (!isTextFile(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only text files can be updated");
        }

        file.setData(content.getBytes());
        return new FileDTO(fileRepository.save(file));
    }

    private boolean isTextFile(DbFile file) {
        String contentType = file.getFileType();
        return contentType.equals("text/plain") ||
                contentType.equals("text/markdown") ||
                contentType.equals("application/json");
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
