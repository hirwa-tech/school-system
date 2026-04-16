package com.hirwa.classprogram.quiz.controller;

import com.hirwa.classprogram.quiz.entity.Question;
import com.hirwa.classprogram.quiz.entity.Quiz;
import com.hirwa.classprogram.quiz.repository.QuestionRepository;
import com.hirwa.classprogram.quiz.service.QuizService;
import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/quiz")
public class QuestionController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/{quizId}/questions")
    public String listQuestions(@PathVariable Long quizId, Authentication auth, Model model) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

    
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quiz.getQuestions());
        
  
        int totalMarks = quiz.getQuestions().stream()
                .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1)
                .sum();
        model.addAttribute("totalMarks", totalMarks);
        
        return "quiz/questions";
    }

    @GetMapping("/{quizId}/question/create")
    public String createQuestionForm(@PathVariable Long quizId, Model model) {
        model.addAttribute("quizId", quizId);
        model.addAttribute("question", new Question());
        return "quiz/question_form";
    }

    @PostMapping("/{quizId}/question/create")
    public String createQuestion(@PathVariable Long quizId, 
                                  @ModelAttribute Question question,
                                  @RequestParam(required = false) List<String> options,
                                  @RequestParam(required = false) String correctAnswer,
                                  @RequestParam(defaultValue = "1") Integer marks,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

       
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        question.setQuiz(quiz);
        question.setCorrectAnswer(correctAnswer);
        question.setMarks(marks != null ? marks : 1);
        

        if (options != null && !options.isEmpty()) {
            question.setOptions(options);
        }

        questionRepository.save(question);
        redirectAttributes.addFlashAttribute("message", "Question added successfully!");
        return "redirect:/quiz/" + quizId + "/questions";
    }

    @GetMapping("/{quizId}/question/{questionId}/edit")
    public String editQuestionForm(@PathVariable Long quizId, @PathVariable Long questionId, Model model) {
        Question question = questionRepository.findById(questionId).orElseThrow();
        model.addAttribute("quizId", quizId);
        model.addAttribute("question", question);
        return "quiz/question_form";
    }

    @PostMapping("/{quizId}/question/{questionId}/edit")
    public String editQuestion(@PathVariable Long quizId,
                               @PathVariable Long questionId,
                               @ModelAttribute Question question,
                               @RequestParam(required = false) List<String> options,
                               @RequestParam(required = false) String correctAnswer,
                               @RequestParam(defaultValue = "1") Integer marks,
                               RedirectAttributes redirectAttributes) {
        Question existingQuestion = questionRepository.findById(questionId).orElseThrow();
        
        existingQuestion.setQuestionText(question.getQuestionText());
        existingQuestion.setType(question.getType());
        existingQuestion.setCorrectAnswer(correctAnswer);
        existingQuestion.setMarks(marks != null ? marks : 1);
        
        if (options != null && !options.isEmpty()) {
            existingQuestion.setOptions(options);
        }

        questionRepository.save(existingQuestion);
        redirectAttributes.addFlashAttribute("message", "Question updated successfully!");
        return "redirect:/quiz/" + quizId + "/questions";
    }

    @GetMapping("/{quizId}/question/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId, 
                                 @PathVariable Long questionId,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.findById(quizId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

       
        if (!quiz.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        questionRepository.deleteById(questionId);
        redirectAttributes.addFlashAttribute("message", "Question deleted successfully!");
        return "redirect:/quiz/" + quizId + "/questions";
    }
}

