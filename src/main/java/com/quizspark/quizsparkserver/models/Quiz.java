package com.quizspark.quizsparkserver.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonBackReference
    private Collection collection;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(name = "choice_1", nullable = false)
    private String choice1;

    @Column(name = "choice_2")
    private String choice2;

    @Column(name = "choice_3")
    private String choice3;

    @Column(name = "choice_4")
    private String choice4;
}
