package com.hirwa.classprogram.quiz.entity;

import com.hirwa.classprogram.classroom.Classroom;
import com.hirwa.classprogram.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    @JsonIgnoreProperties({"homeworks", "notes", "quizzes", "students", "teacher", "password", "createdAt"})
    private Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"password", "email", "role", "firstName", "lastName", "enabled", "temporary", "classrooms", "teacher"})
    private User teacher;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"quiz"})
    private List<Question> questions;

    private Integer timeLimit; 

    private LocalDateTime deadline;

    @Column(nullable = false)
    private String password; 

    private Integer attemptLimit = 1; 

    @Column(nullable = false)
    private boolean antiCheatEnabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
