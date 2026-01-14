package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class DbFileContent {
    @Id
    @Column(name = "file_id")
    private UUID id;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "file_id")
    private DbFile file;

    public DbFileContent() {
    }

    public DbFileContent(DbFile file, byte[] data) {
        this.file = file;
        this.data = data;
    }
}