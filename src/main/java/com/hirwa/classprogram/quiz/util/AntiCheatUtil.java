package com.hirwa.classprogram.quiz.util;

import com.hirwa.classprogram.quiz.entity.QuizAttempt;
import org.springframework.stereotype.Component;

@Component
public class AntiCheatUtil {

    public boolean detectCheating(QuizAttempt attempt) {
        if (attempt.getEndTime() == null || attempt.getStartTime() == null) {
            return false;
        }

        long duration = java.time.Duration.between(attempt.getStartTime(), attempt.getEndTime()).toMinutes();
        Integer timeLimit = attempt.getQuiz().getTimeLimit();

        if (timeLimit != null && duration < timeLimit * 0.1) {
            return true; 
        }

       
        return false;
    }
}