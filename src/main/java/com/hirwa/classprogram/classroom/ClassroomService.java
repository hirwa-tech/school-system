package com.hirwa.classprogram.classroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hirwa.classprogram.user.User;

import java.util.List;
import java.util.Optional;

@Service
public class ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

public Classroom save(Classroom classroom) {
       
        if (classroom.getPassword() != null && !classroom.getPassword().isEmpty() 
            && !classroom.getPassword().startsWith("$2")) {
            classroom.setPassword(passwordEncoder.encode(classroom.getPassword()));
        }
        return classroomRepository.save(classroom);
    }

    public Optional<Classroom> findById(Long id) {
        return classroomRepository.findById(id);
    }

    public List<Classroom> findByTeacher(User teacher) {
        return classroomRepository.findByTeacherId(teacher.getId());
    }

    public List<Classroom> findByStudent(User student) {
        return classroomRepository.findByStudentsId(student.getId());
    }

    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }

    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAll();
    }

    public List<Classroom> getPublicClassrooms() {
       
        return classroomRepository.findAll();
    }

    public void deleteById(Long id) {
        classroomRepository.deleteById(id);
    }

    public boolean checkPassword(Classroom classroom, String password) {
        return passwordEncoder.matches(password, classroom.getPassword());
    }
}