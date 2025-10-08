package com.quizspark.quizsparkserver.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private UUID id;
    private String username;
    private String password;
    // Relationship: one user can own many collections
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Collection> collections = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.id = UUID.randomUUID();
        this.collections = new ArrayList<>();
    }
}
