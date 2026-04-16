package com.hirwa.classprogram.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"password", "email", "role", "firstName", "lastName", "enabled", "temporary", "hibernateLazyInitializer", "handler", "classrooms", "teacher"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean temporary = false;

    public enum Role {
        TEACHER, STUDENT, DS
    }
}
