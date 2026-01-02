package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class DbFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToMany(mappedBy = "files")
    private List<QuestionBank> questionBanks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_file_id")
    private DbFile originFile;

    @OneToMany(mappedBy = "originFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DbFile> derivedFiles = new ArrayList<>();

    // Default constructor
    public DbFile() {
        this.uploadDate = LocalDateTime.now();
    }

    // Parameterized constructor
    public DbFile(String fileName, String fileType, byte[] data, User user) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.user = user;
        this.uploadDate = LocalDateTime.now();
    }
}
