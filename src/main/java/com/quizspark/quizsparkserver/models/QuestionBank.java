package com.quizspark.quizsparkserver.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "question_bank", indexes = {
        @Index(name = "idx_question_bank_user_id", columnList = "user_id"),
        @Index(name = "idx_question_bank_status", columnList = "status"),
        @Index(name = "idx_question_bank_access", columnList = "access")
})
@Getter
@Setter
@NoArgsConstructor
public class QuestionBank {
    public enum Status {
        DRAFT, PUBLISHED
    }

    public enum Access {
        PUBLIC, PRIVATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Access access;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Date createdAt = new Date();

    private Float rating;

    // Relationship: One QuestionBank can contain many Questions
    @OneToMany(mappedBy = "questionBank", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(name = "question_bank_files", joinColumns = @JoinColumn(name = "question_bank_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (question_bank_id) REFERENCES question_bank(id) ON DELETE CASCADE")), inverseJoinColumns = @JoinColumn(name = "file_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (file_id) REFERENCES db_file(id) ON DELETE CASCADE")))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<DbFile> files = new ArrayList<>();

    public Optional<Tag> getTagByName(String name) {
        return tags.stream().filter(tag -> tag.getName().equals(name)).findFirst();
    }

    public Optional<Tag> getTagById(UUID id) {
        return tags.stream().filter(tag -> tag.getId().equals(id)).findFirst();
    }
}
