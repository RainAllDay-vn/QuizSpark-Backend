package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.banks.QuestionBankCreationDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankDTO;
import com.quizspark.quizsparkserver.dto.banks.QuestionBankUpdateDTO;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.FileService;
import com.quizspark.quizsparkserver.services.QuestionService;
import com.quizspark.quizsparkserver.dto.FileDTO;
import com.quizspark.quizsparkserver.models.DbFile;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
public class QuestionBankController {
    private final QuestionService questionService;
    private final FileService fileService;

    @Autowired
    public QuestionBankController(QuestionService questionService, FileService fileService) {
        this.questionService = questionService;
        this.fileService = fileService;
    }

    @GetMapping("/public")
    public ResponseEntity<List<QuestionBankDTO>> getPublicQuestionBanks() {
        List<QuestionBankDTO> body = questionService.getPublicQuestionBanks();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<QuestionBankDTO>> getUserQuestionBanks(@AuthenticationPrincipal User user) {
        List<QuestionBankDTO> body = new ArrayList<>(questionService.getQuestionBankByUser());
        if (user.getRole().equals("ROLE_ADMIN")) {
            body.addAll(questionService.getAdminBank());
        }
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/single/{id}")
    public ResponseEntity<QuestionBankDTO> getSingleQuestionBank(@PathVariable String id) {
        QuestionBankDTO body = questionService.getQuestionBankById(id).appendQuestions();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPublicQuestionBank(@RequestBody @Valid QuestionBankCreationDTO creationDTO) {
        QuestionBankDTO body = questionService.savePublicQuestionBank(creationDTO);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<QuestionBankDTO> createQuestionBank(@RequestBody @Valid QuestionBankCreationDTO creationDTO) {
        QuestionBankDTO body = questionService.saveQuestionBank(creationDTO);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PutMapping("/single/{id}")
    public ResponseEntity<QuestionBankDTO> updateSingleQuestionBank(@PathVariable String id,
            @RequestBody @Valid QuestionBankUpdateDTO payload) {
        QuestionBankDTO body = questionService.updateQuestionBank(id, payload);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @DeleteMapping("/single/{id}")
    public ResponseEntity<Object> deleteSingleQuestionBank(@PathVariable String id) {
        questionService.deleteQuestionBank(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/single/{id}/files")
    public ResponseEntity<FileDTO> uploadFile(@PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        FileDTO fileDto = fileService.storeFile(id, file);
        return new ResponseEntity<>(fileDto, HttpStatus.OK);
    }

    @DeleteMapping("/single/{id}/files/{fileId}")
    public ResponseEntity<Object> deleteFile(@PathVariable String id, @PathVariable String fileId) {
        fileService.deleteFile(id, fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/single/{id}/files/{fileId}/view")
    public ResponseEntity<byte[]> viewFile(@PathVariable String id, @PathVariable String fileId) {
        DbFile dbFile = fileService.getFileEntity(fileId);

        if (!dbFile.getQuestionBank().getId().toString().equals(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String fileType = dbFile.getFileType();
        if (!fileType.equals("application/pdf")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dbFile.getFileName() + "\"")
                .body(dbFile.getData());
    }

    @GetMapping("/single/{id}/files/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id, @PathVariable String fileId) {
        DbFile dbFile = fileService.getFileEntity(fileId);

        if (!dbFile.getQuestionBank().getId().toString().equals(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                .body(dbFile.getData());
    }
}
