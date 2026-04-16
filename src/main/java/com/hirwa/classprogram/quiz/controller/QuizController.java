package com.hirwa.classprogram.quiz.controller;

import com.hirwa.classprogram.classroom.Classroom;
import com.hirwa.classprogram.classroom.ClassroomService;
import com.hirwa.classprogram.quiz.entity.*;
import com.hirwa.classprogram.quiz.service.QuizService;
import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @GetMapping("/classroom/{classroomId}")
    public String listQuizzes(@PathVariable Long classroomId, Authentication auth, Model model) {
        Classroom classroom = classroomService.findById(classroomId).orElseThrow();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        if (!hasAccess(classroom, user)) {
            return "redirect:/dashboard";
        }

        boolean isTeacher = user.getRole() == User.Role.TEACHER && classroom.getTeacher().getId().equals(user.getId());
        var quizzes = quizService.findByClassroomId(classroomId);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("classroom", classroom);
        model.addAttribute("user", user);
        model.addAttribute("isTeacher", isTeacher);
        return "quiz/list";
    }

    @GetMapping("/create/{classroomId}")
    public String createForm(@PathVariable Long classroomId, Model model) {
        model.addAttribute("quiz", new Quiz());
        model.addAttribute("classroomId", classroomId);
        return "quiz/create";
    }

    @PostMapping("/create/{classroomId}")
    public String create(@PathVariable Long classroomId, @ModelAttribute Quiz quiz, Authentication auth, RedirectAttributes redirectAttributes) {
        Classroom classroom = classroomService.findById(classroomId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (teacher.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        quiz.setClassroom(classroom);
        quiz.setTeacher(teacher);
        quizService.save(quiz);
        redirectAttributes.addFlashAttribute("message", "Quiz created successfully!");
        return "redirect:/quiz/" + quiz.getId() + "/questions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        Quiz quiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("classroomId", quiz.getClassroom().getId());
        return "quiz/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Quiz quiz, Authentication auth, RedirectAttributes redirectAttributes) {
        Quiz existingQuiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (!existingQuiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        existingQuiz.setTitle(quiz.getTitle());
        existingQuiz.setDescription(quiz.getDescription());
        existingQuiz.setTimeLimit(quiz.getTimeLimit());
        existingQuiz.setDeadline(quiz.getDeadline());
        existingQuiz.setAntiCheatEnabled(quiz.isAntiCheatEnabled());
        
        if (quiz.getPassword() != null && !quiz.getPassword().isEmpty()) {
            existingQuiz.setPassword(quiz.getPassword());
        }
        
        quizService.save(existingQuiz);
        redirectAttributes.addFlashAttribute("message", "Quiz updated successfully!");
        return "redirect:/quiz/" + id + "/questions";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        Long classroomId = quiz.getClassroom().getId();
        quizService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Quiz deleted successfully!");
        return "redirect:/classroom/" + classroomId;
    }

    @GetMapping("/{id}/start")
    public String startQuizForm(@PathVariable Long id, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();

        if (!quiz.getClassroom().getStudents().contains(student)) {
            return "redirect:/dashboard";
        }

        int attemptLimit = quiz.getAttemptLimit() != null ? quiz.getAttemptLimit() : 3;
        long currentAttempts = quizService.findAttemptsByQuizId(quiz.getId()).stream()
                .filter(a -> a.getStudent().getId().equals(student.getId()) && a.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS)
                .count();
        if (currentAttempts >= attemptLimit) {
            redirectAttributes.addFlashAttribute("error", "Maximum completed attempts (" + attemptLimit + ") reached for this quiz!");
            return "redirect:/quiz/classroom/" + quiz.getClassroom().getId();
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("classroom", quiz.getClassroom());
        return "quiz/start";
    }

    
    @PostMapping("/{id}/start/submit")
    public String startQuiz(@PathVariable Long id, @RequestParam String password, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();

        if (!quiz.getClassroom().getStudents().contains(student)) {
            return "redirect:/dashboard";
        }

        if (!quizService.checkPassword(quiz, password)) {
            redirectAttributes.addFlashAttribute("error", "❌ Invalid access password. Please check with your teacher and try again.");
            return "redirect:/quiz/" + id + "/start";
        }

        if (quiz.getDeadline() != null && LocalDateTime.now().isAfter(quiz.getDeadline())) {
            redirectAttributes.addFlashAttribute("error", "⏰ Quiz deadline has passed! Contact your teacher.");
            return "redirect:/quiz/classroom/" + quiz.getClassroom().getId();
        }

        if (quizService.hasAttempted(quiz.getId(), student.getId())) {
            redirectAttributes.addFlashAttribute("error", "⚠️ You already have an active or completed attempt for this quiz. Check your history.");
            return "redirect:/quiz/classroom/" + quiz.getClassroom().getId();
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);
        quizService.saveAttempt(attempt);

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", quiz.getQuestions() != null ? quiz.getQuestions() : List.of());

    
        long currentTimeMillis = System.currentTimeMillis();
  
        int timeLimit = (quiz.getTimeLimit() != null) ? quiz.getTimeLimit() : 60;
        long sessionEndMillis = currentTimeMillis + (timeLimit * 60L * 1000L);

        long finalDeadlineMillis = sessionEndMillis;
        if (quiz.getDeadline() != null) {
            long globalDeadlineMillis = quiz.getDeadline().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            finalDeadlineMillis = Math.min(sessionEndMillis, globalDeadlineMillis);
        }
        
        model.addAttribute("deadlineMillis", finalDeadlineMillis);

        return "quiz/attempt";
    }

    @PostMapping("/{id}/submit")
    public String submitQuiz(@PathVariable Long id, 
                             @RequestParam Map<String, String> allParams, 
                             @RequestParam(required = false) String submissionReason, 
                             Authentication auth, 
                             RedirectAttributes redirectAttributes) {
        
        Quiz quiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();
        QuizAttempt attempt = quizService.findAttemptByQuizAndStudent(quiz.getId(), student.getId()).orElseThrow();
        
        if (submissionReason != null && !submissionReason.isEmpty()) {
            System.out.println("SECURITY ALERT: Quiz " + id + " submitted by " + username + " via: " + submissionReason);
        }

        if (attempt.getStatus() == QuizAttempt.AttemptStatus.SUBMITTED || 
            attempt.getStatus() == QuizAttempt.AttemptStatus.GRADED ||
            attempt.getStatus() == QuizAttempt.AttemptStatus.PENDING_MANUAL_REVIEW) {
            return "redirect:/quiz/result/" + attempt.getId();
        }
        
        Map<Long, String> answers = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("answers["))
                .collect(Collectors.toMap(
                    e -> Long.parseLong(e.getKey().replace("answers[", "").replace("]", "")),
                    Map.Entry::getValue
                ));
        
        attempt.setAnswers(answers);
        attempt.setEndTime(LocalDateTime.now());
        
        double autoEarnedMarks = quizService.autoGrade(attempt);
        boolean hasOpenQuestions = quiz.getQuestions().stream()
                .anyMatch(q -> q.getType() == Question.QuestionType.OPEN_QUESTION);
        
        int totalMarks = quizService.calculateTotalMarks(quiz);
        attempt.setTotalMarks((double) totalMarks);
        attempt.setScore(autoEarnedMarks);

        if (!hasOpenQuestions) {
            attempt.setStatus(QuizAttempt.AttemptStatus.GRADED);
        } else {
            attempt.setStatus(QuizAttempt.AttemptStatus.PENDING_MANUAL_REVIEW);
        }

        quizService.saveAttempt(attempt);
        redirectAttributes.addFlashAttribute("message", "Quiz submitted successfully!");
        return "redirect:/quiz/result/" + attempt.getId();
    }

    @GetMapping("/result/{attemptId}")
    public String viewResult(@PathVariable Long attemptId, Authentication auth, Model model) {
        QuizAttempt attempt = quizService.findAttemptById(attemptId).orElseThrow();
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();
        
        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/dashboard";
        }
        
        Quiz quiz = attempt.getQuiz();
        Map<Long, Question> questions = new HashMap<>();
        Map<Long, String> answers = attempt.getAnswers();
        
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                questions.put(q.getId(), q);
            }
        }
        
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers != null ? answers : new HashMap<Long, String>());
        model.addAttribute("feedbacks", attempt.getFeedbacks() != null ? attempt.getFeedbacks() : new HashMap<Long, String>());
        
        return "quiz/result";
    }

    @GetMapping("/history")
    public String quizHistory(Authentication auth, Model model) {
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();
        List<QuizAttempt> attempts = quizService.findAttemptsByStudentUsername(username);
        model.addAttribute("attempts", attempts);
        model.addAttribute("student", student);
        return "quiz/history";
    }

    @PostMapping("/{id}/delete-graded")
    public String deleteGradedAttempts(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        quizService.deleteGradedAttemptsByQuizId(id);
        redirectAttributes.addFlashAttribute("message", "All graded attempts deleted successfully!");
        return "redirect:/quiz/" + id + "/attempts";
    }

    @PostMapping("/{quizId}/teacher/reset/{studentId}")
    public String resetStudentAttempt(@PathVariable Long quizId, @PathVariable Long studentId, Authentication auth, RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        Optional<QuizAttempt> attemptOpt = quizService.findAttemptByQuizAndStudent(quizId, studentId);
        if (attemptOpt.isPresent()) {
            quizService.deleteAttempt(attemptOpt.get().getId());
            redirectAttributes.addFlashAttribute("message", "✅ Student attempt reset! They can now retake the quiz.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No attempt found for this student on this quiz.");
        }
        return "redirect:/quiz/" + quizId + "/attempts";
    }

    private boolean hasAccess(Classroom classroom, User user) {
        if (user.getRole() == User.Role.TEACHER) {
            return classroom.getTeacher().getId().equals(user.getId());
        } else {
            return classroom.getStudents().contains(user);
        }
    }
}