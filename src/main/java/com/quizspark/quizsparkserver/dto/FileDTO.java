package com.quizspark.quizsparkserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.DbFile;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDTO {
    private UUID id;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadDate;
    private List<FileDTO> derivedFiles;

    public FileDTO(DbFile dbFile) {
        this(dbFile, false);
    }

    private FileDTO(DbFile dbFile, boolean isDerived) {
        this.id = dbFile.getId();
        this.fileName = dbFile.getFileName();
        this.fileType = dbFile.getFileType();
        this.uploadDate = dbFile.getUploadDate();

        if (!isDerived && dbFile.getDerivedFiles() != null) {
            this.derivedFiles = dbFile.getDerivedFiles().stream()
                    .map(file -> new FileDTO(file, true))
                    .toList();
        }
    }
}
