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

    @OneToOne(mappedBy = "file", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private DbFileContent content;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(mappedBy = "files")
    private List<QuestionBank> questionBanks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_file_id")
    private DbFile originFile;

    @OneToMany(mappedBy = "originFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DbFile> derivedFiles = new ArrayList<>();

    @OneToMany(mappedBy = "file")
    private List<ChatItem> chatItems = new ArrayList<>();

    // Default constructor
    public DbFile() {
        this.uploadDate = LocalDateTime.now();
    }

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean canEdit = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isHiddent = false;

    @Column(nullable = false)
    private boolean isEmbedded = false;
    
    // Parameterized constructor
    public DbFile(String fileName, String fileType, byte[] data, User user) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.user = user;
        this.uploadDate = LocalDateTime.now();
        this.setData(data);
    }

    public byte[] getData() {
        return content != null ? content.getData() : null;
    }

    public void setData(byte[] data) {
        if (this.content == null) {
            this.content = new DbFileContent(this, data);
        } else {
            this.content.setData(data);
        }
    }
}
