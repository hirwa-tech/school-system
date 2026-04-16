package com.hirwa.classprogram.quiz.controller;

import com.hirwa.classprogram.quiz.entity.Question;
import com.hirwa.classprogram.quiz.entity.Quiz;
import com.hirwa.classprogram.quiz.entity.QuizAttempt;
import com.hirwa.classprogram.quiz.service.QuizService;
import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quiz")
public class GradingController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    @GetMapping("/{quizId}/attempts")
    public String viewAttempts(@PathVariable Long quizId, Authentication auth, Model model) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

      
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        List<QuizAttempt> attempts = quizService.findAttemptsByQuizId(quizId);
        
    
        long totalAttempts = attempts.size();
        long gradedAttempts = attempts.stream().filter(a -> a.getStatus() == QuizAttempt.AttemptStatus.GRADED).count();
        double avgScore = attempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToDouble(QuizAttempt::getScore)
                .average()
                .orElse(0.0);
        double highestScore = attempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToDouble(QuizAttempt::getScore)
                .max()
                .orElse(0.0);
        double lowestScore = attempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToDouble(QuizAttempt::getScore)
                .min()
                .orElse(0.0);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("attempts", attempts);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("gradedAttempts", gradedAttempts);
        model.addAttribute("avgScore", avgScore);
        model.addAttribute("highestScore", highestScore);
        model.addAttribute("lowestScore", lowestScore);
        return "quiz/attempts";
    }

    @GetMapping("/{quizId}/grade-open")
    public String bulkGradeOpenQuestions(@PathVariable Long quizId, Authentication auth, Model model) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

     
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        List<QuizAttempt> attempts = quizService.findAttemptsByQuizId(quizId);
        List<Question> openQuestions = quiz.getQuestions().stream()
                .filter(q -> q.getType() == Question.QuestionType.OPEN_QUESTION)
                .collect(Collectors.toList());

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempts", attempts);
        model.addAttribute("openQuestions", openQuestions);
        return "quiz/grade_open_bulk";
    }

    @PostMapping("/{quizId}/grade-open")
    public String bulkGradeOpenQuestionsSubmit(@PathVariable Long quizId,
                                              @RequestParam Map<String, String> allParams,
                                              Authentication auth,
                                              RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        List<QuizAttempt> attempts = quizService.findAttemptsByQuizId(quizId);

        for (QuizAttempt attempt : attempts) {
            double manualEarnedMarks = 0;
            Map<Long, String> feedbacks = attempt.getFeedbacks() != null ? new HashMap<>(attempt.getFeedbacks()) : new HashMap<>();

            for (Question q : quiz.getQuestions()) {
                if (q.getType() == Question.QuestionType.OPEN_QUESTION) {
                    String scoreParam = "score_" + q.getId() + "_" + attempt.getId();
                    String feedbackParam = "feedback_" + q.getId() + "_" + attempt.getId();

                    if (allParams.containsKey(scoreParam)) {
                        try {
                            double marks = Double.parseDouble(allParams.get(scoreParam));
                            int maxMarks = q.getMarks() != null ? q.getMarks() : 1;
                            manualEarnedMarks += Math.min(marks, maxMarks);
                        } catch (NumberFormatException e) {
                           
                        }
                    }

                    if (allParams.containsKey(feedbackParam) && !allParams.get(feedbackParam).trim().isEmpty()) {
                        feedbacks.put(q.getId(), allParams.get(feedbackParam));
                    }
                }
            }

       
            double autoEarnedMarks = quizService.autoGrade(attempt);
            double totalEarnedMarks = autoEarnedMarks + manualEarnedMarks;
            int totalPossibleMarks = quizService.calculateTotalMarks(quiz);
            
           
            double finalScore = totalEarnedMarks;

            attempt.setScore(finalScore);
            attempt.setTotalMarks((double) totalPossibleMarks);
            attempt.setFeedbacks(feedbacks);
            attempt.setStatus(QuizAttempt.AttemptStatus.GRADED);
            quizService.saveAttempt(attempt);
        }

        redirectAttributes.addFlashAttribute("message", "All grades saved successfully!");
        return "redirect:/quiz/" + quizId + "/attempts";
    }

    @GetMapping("/{quizId}/attempt/{attemptId}/grade")
    public String gradeAttemptForm(@PathVariable Long quizId, @PathVariable Long attemptId, Authentication auth, Model model) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        QuizAttempt attempt = quizService.findAttemptById(attemptId).orElseThrow();
        
        Map<Long, Question> questions = new HashMap<>();
        Map<Long, String> answers = attempt.getAnswers();
        
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                questions.put(q.getId(), q);
            }
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers != null ? answers : new HashMap<Long, String>());
        model.addAttribute("feedbacks", attempt.getFeedbacks() != null ? attempt.getFeedbacks() : new HashMap<Long, String>());
        return "quiz/grade_form";
    }

    @PostMapping("/{quizId}/attempt/{attemptId}/grade")
    public String gradeAttempt(@PathVariable Long quizId,
                              @PathVariable Long attemptId,
                              @RequestParam Map<String, String> allParams,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

    
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        QuizAttempt attempt = quizService.findAttemptById(attemptId).orElseThrow();
     
        double manualEarnedMarks = 0;
        Map<Long, String> feedbacks = new HashMap<>();
        
        for (Question q : quiz.getQuestions()) {
            if (q.getType() == Question.QuestionType.OPEN_QUESTION) {
                String scoreParam = "score_" + q.getId();
                String feedbackParam = "feedback_" + q.getId();
                
                if (allParams.containsKey(scoreParam)) {
                    try {
                        double marks = Double.parseDouble(allParams.get(scoreParam));
                        
                        int maxMarks = q.getMarks() != null ? q.getMarks() : 1;
                        manualEarnedMarks += Math.min(marks, maxMarks);
                    } catch (NumberFormatException e) {
                        
                    }
                }
                
                if (allParams.containsKey(feedbackParam) && !allParams.get(feedbackParam).trim().isEmpty()) {
                    feedbacks.put(q.getId(), allParams.get(feedbackParam));
                }
            }
        }

       
        attempt.setFeedbacks(feedbacks);

        double autoEarnedMarks = quizService.autoGrade(attempt);
        
      
        double totalEarnedMarks = autoEarnedMarks + manualEarnedMarks;
        int totalPossibleMarks = quizService.calculateTotalMarks(quiz);
        double finalScore = totalEarnedMarks;
        
        attempt.setScore(finalScore);
        attempt.setTotalMarks((double) totalPossibleMarks);
        attempt.setStatus(QuizAttempt.AttemptStatus.GRADED);
        quizService.saveAttempt(attempt);

        redirectAttributes.addFlashAttribute("message", "Grade saved successfully!");
        return "redirect:/quiz/" + quizId + "/attempts";
    }


}

