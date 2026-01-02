package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.FileDTO;
import com.quizspark.quizsparkserver.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<FileDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        FileDTO fileDto = fileService.storeFile(file);
        return new ResponseEntity<>(fileDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FileDTO>> getUserFiles() {
        List<FileDTO> files = fileService.getUserFiles();
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
