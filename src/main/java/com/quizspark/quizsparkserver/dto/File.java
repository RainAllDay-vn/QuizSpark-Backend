package com.quizspark.quizsparkserver.dto;

import com.quizspark.quizsparkserver.models.DbFile;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class File {
    private UUID id;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadDate;

    public File(DbFile dbFile) {
        this.id = dbFile.getId();
        this.fileName = dbFile.getFileName();
        this.fileType = dbFile.getFileType();
        this.uploadDate = dbFile.getUploadDate();
    }
}
