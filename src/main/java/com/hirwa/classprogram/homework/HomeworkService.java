package com.hirwa.classprogram.homework;
import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.classroom.Classroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HomeworkService {

    @Autowired
    private HomeworkRepository homeworkRepository;

    public Homework save(Homework homework) {
        return homeworkRepository.save(homework);
    }

    public Optional<Homework> findById(Long id) {
        return homeworkRepository.findById(id);
    }

    public List<Homework> findByClassroom(Classroom classroom) {
        return homeworkRepository.findByClassroomId(classroom.getId());
    }

    public List<Homework> findByClassroomId(Long classroomId) {
        return homeworkRepository.findByClassroomId(classroomId);
    }

    public List<Homework> findByTeacher(User teacher) {
        return homeworkRepository.findByTeacherId(teacher.getId());
    }

    public void deleteById(Long id) {
        homeworkRepository.deleteById(id);
    }
}