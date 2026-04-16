package com.hirwa.classprogram.classroom;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;
import com.hirwa.classprogram.homework.Homework;
import com.hirwa.classprogram.notes.Note;
import com.hirwa.classprogram.quiz.entity.Quiz;
import com.hirwa.classprogram.user.User;

@Entity
@Table(name = "classrooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"password", "email", "role", "firstName", "lastName", "enabled", "temporary", "classrooms", "teacher", "hibernateLazyInitializer", "handler"})
    private User teacher;

    @ManyToMany
    @JoinTable(
        name = "classroom_students",
        joinColumns = @JoinColumn(name = "classroom_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnoreProperties({"password", "email", "role", "firstName", "lastName", "enabled", "temporary", "classrooms", "teacher", "hibernateLazyInitializer", "handler"})
    private List<User> students;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"classroom"})
    private List<Note> notes;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"classroom"})
    private List<Homework> homeworks;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"classroom"})
    private List<Quiz> quizzes;

    @Column(nullable = false)
    private String password; 

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

