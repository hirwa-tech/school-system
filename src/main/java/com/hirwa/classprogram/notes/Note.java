package com.hirwa.classprogram.notes;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.classroom.Classroom;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    @JsonIgnoreProperties({"homeworks", "notes", "quizzes", "students", "teacher", "password", "createdAt"})
    private Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"password", "email", "role", "firstName", "lastName", "enabled", "temporary", "classrooms", "teacher"})
    private User teacher;

    private String fileName;
    private String fileType;
    private Long fileSize;
    
    @Column(columnDefinition = "BYTEA")
    private byte[] fileData;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}

