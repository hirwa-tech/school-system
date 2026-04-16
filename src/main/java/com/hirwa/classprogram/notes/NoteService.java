package com.hirwa.classprogram.notes;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.classroom.Classroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public Note save(Note note) {
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public Optional<Note> findById(Long id) {
        return noteRepository.findById(id);
    }

    public List<Note> findByClassroom(Classroom classroom) {
        return noteRepository.findByClassroomId(classroom.getId());
    }

    public List<Note> findByClassroomId(Long classroomId) {
        return noteRepository.findByClassroomId(classroomId);
    }

    public List<Note> findByTeacher(User teacher) {
        return noteRepository.findByTeacherId(teacher.getId());
    }

    public void deleteById(Long id) {
        noteRepository.deleteById(id);
    }
}