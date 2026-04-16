package com.hirwa.classprogram.quiz.repository;

import com.hirwa.classprogram.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByQuizId(Long quizId);

    List<QuizAttempt> findByStudentId(Long studentId);

    Optional<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    @Query("SELECT a FROM QuizAttempt a WHERE a.student.username = :username")
    List<QuizAttempt> findByStudentUsername(String username);

    void deleteByQuizIdAndStatus(Long quizId, QuizAttempt.AttemptStatus status);

}
