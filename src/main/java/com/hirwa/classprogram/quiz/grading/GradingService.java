package com.hirwa.classprogram.quiz.grading;

import com.hirwa.classprogram.quiz.entity.Question;
import com.hirwa.classprogram.quiz.entity.QuizAttempt;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GradingService {

    
    public double gradeQuiz(QuizAttempt attempt, Map<Long, Question> questions) {
        Map<Long, String> answers = attempt.getAnswers();
        
     
        double totalMarks = 0;
        double earnedMarks = 0;
        
        for (Question question : questions.values()) {
            int questionMarks = question.getMarks() != null ? question.getMarks() : 1;
            totalMarks += questionMarks;
            
            String userAnswer = answers.get(question.getId());
            if (userAnswer != null && question.getCorrectAnswer() != null) {
             
                if (question.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
                    if (userAnswer.equals(question.getCorrectAnswer())) {
                        earnedMarks += questionMarks;
                    }
                } else if (question.getType() == Question.QuestionType.FILL_IN_THE_GAP) {
                    if (userAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                        earnedMarks += questionMarks;
                    }
         }
           
            }
        }
        
        if (totalMarks == 0) {
            return 0;
        }
        
        return (earnedMarks / totalMarks) * 100;
    }
    
    

    public int calculateTotalMarks(Map<Long, Question> questions) {
        return questions.values().stream()
                .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1)
                .sum();
    }
}

