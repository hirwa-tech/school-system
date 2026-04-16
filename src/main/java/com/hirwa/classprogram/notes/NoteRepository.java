package com.hirwa.classprogram.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByClassroomId(Long classroomId);

    List<Note> findByTeacherId(Long teacherId);
}