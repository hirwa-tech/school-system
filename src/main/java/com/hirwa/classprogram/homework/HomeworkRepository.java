package com.hirwa.classprogram.homework;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    List<Homework> findByClassroomId(Long classroomId);

    List<Homework> findByTeacherId(Long teacherId);
}