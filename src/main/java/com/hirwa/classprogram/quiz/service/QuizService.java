package com.hirwa.classprogram.quiz.service;

import com.hirwa.classprogram.quiz.entity.Question;
import com.hirwa.classprogram.quiz.entity.Quiz;
import com.hirwa.classprogram.quiz.entity.QuizAttempt;
import com.hirwa.classprogram.quiz.repository.QuizRepository;
import com.hirwa.classprogram.quiz.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Quiz save(Quiz quiz) {
        if (quiz.getPassword() != null && !quiz.getPassword().isEmpty()) {
            quiz.setPassword(passwordEncoder.encode(quiz.getPassword()));
        }
        return quizRepository.save(quiz);
    }

    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    public List<Quiz> findByClassroomId(Long classroomId) {
        return quizRepository.findByClassroomId(classroomId);
    }

    public List<Quiz> findByTeacherId(Long teacherId) {
        return quizRepository.findByTeacherId(teacherId);
    }

    public void deleteById(Long id) {
        quizRepository.deleteById(id);
    }

    public QuizAttempt saveAttempt(QuizAttempt attempt) {
        return quizAttemptRepository.save(attempt);
    }

    public Optional<QuizAttempt> findAttemptById(Long id) {
        return quizAttemptRepository.findById(id);
    }

    public List<QuizAttempt> findAttemptsByQuizId(Long quizId) {
        return quizAttemptRepository.findByQuizId(quizId);
    }

    public Optional<QuizAttempt> findAttemptByQuizAndStudent(Long quizId, Long studentId) {
        return quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId);
    }

    public boolean checkPassword(Quiz quiz, String password) {
        return passwordEncoder.matches(password, quiz.getPassword());
    }

    public boolean hasAttempted(Long quizId, Long studentId) {
        return quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId).isPresent();
    }

    
    public double autoGrade(QuizAttempt attempt) {
        Quiz quiz = attempt.getQuiz();
        double totalEarnedMarks = 0;

        for (Question question : quiz.getQuestions()) {
            
            if (question.getType() == Question.QuestionType.OPEN_QUESTION) {
                continue;
            }
            
            String studentAnswer = attempt.getAnswers().get(question.getId());
            if (studentAnswer != null && question.getCorrectAnswer() != null) {
                int questionMarks = question.getMarks() != null ? question.getMarks() : 1;
                switch (question.getType()) {
                    case MULTIPLE_CHOICE:
                        if (studentAnswer.trim().equals(question.getCorrectAnswer().trim())) {
                            totalEarnedMarks += questionMarks;
                        }
                        break;
                    case FILL_IN_THE_GAP:
                        if (studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                            totalEarnedMarks += questionMarks;
                        }
                        break;
                    case OPEN_QUESTION:
                        
                        break;
                }
            }
        }

        return totalEarnedMarks;
    }
    
    
    public int calculateTotalMarks(Quiz quiz) {
        return quiz.getQuestions().stream()
                .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1)
                .sum();
    }

    
    public List<QuizAttempt> findAttemptsByStudentUsername(String username) {
        return quizAttemptRepository.findByStudentUsername(username);
    }

@Transactional
    public void deleteGradedAttemptsByQuizId(Long quizId) {
        quizAttemptRepository.deleteByQuizIdAndStatus(quizId, QuizAttempt.AttemptStatus.GRADED);
    }



    
    public void deleteAttempt(Long id) {
        quizAttemptRepository.deleteById(id);
    }
}

