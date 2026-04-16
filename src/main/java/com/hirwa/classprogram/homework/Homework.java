package com.hirwa.classprogram.homework;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.classroom.Classroom;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "homeworks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    @JsonIgnoreProperties({"homeworks", "notes", "quizzes", "students", "teacher", "password", "createdAt"})
    private Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"password", "email", "role", "firstName", "lastName", "enabled", "temporary", "classrooms", "teacher"})
    private User teacher;

    private LocalDateTime dueDate;

    private String fileName;
    private String fileType;
    private Long fileSize;
    
    @Column(columnDefinition = "BYTEA")
    private byte[] fileData;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

