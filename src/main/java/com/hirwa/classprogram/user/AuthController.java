package com.hirwa.classprogram.user;

import com.hirwa.classprogram.classroom.Classroom;
import com.hirwa.classprogram.classroom.ClassroomService;
import com.hirwa.classprogram.quiz.entity.QuizAttempt;
import com.hirwa.classprogram.quiz.service.QuizService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        
        if (auth != null && auth.getName() != null) {
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == User.Role.STUDENT && user.isTemporary()) {
                    
                    String username = user.getUsername();
                    
                    
                    List<QuizAttempt> attempts = quizService.findAttemptsByStudentUsername(username);
                    for (QuizAttempt attempt : attempts) {
                        quizService.deleteAttempt(attempt.getId());
                    }
                    
                   
                    userService.delete(user.getId());
                    
                    redirectAttributes.addFlashAttribute("message", "Temporary account deleted.");
                }
            }
        }
        
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String role,
                           Model model) {
        if (userService.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        if (userService.existsByEmail(email)) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(User.Role.valueOf(role.toUpperCase()));
        user.setTemporary(false); 
        userService.save(user);

        return "redirect:/login?registered";
    }

  
    @PostMapping("/student/quick-login")
    public String quickStudentLogin(@RequestParam String username,
                                    @RequestParam String classroomCode,
                                    @RequestParam Long classroomId,
                                    Model model,
                                    HttpServletRequest request) {
       
        Optional<Classroom> classroomOpt = classroomService.findById(classroomId);
        if (classroomOpt.isEmpty()) {
            model.addAttribute("error", "Invalid classroom");
            return "login";
        }
        
        Classroom classroom = classroomOpt.get();
        
       
        if (!classroomService.checkPassword(classroom, classroomCode)) {
            model.addAttribute("error", "Invalid classroom password");
            return "login";
        }

        
        String uniqueUsername = username + "_" + System.currentTimeMillis();
        
      
        User student = new User();
        student.setUsername(uniqueUsername);
        student.setPassword(passwordEncoder.encode("temp_" + uniqueUsername)); 
        student.setEmail(uniqueUsername + "@student.lms");
        student.setFirstName(username);
        student.setLastName("Student");
        student.setRole(User.Role.STUDENT);
        student.setTemporary(true); 
        student = userService.save(student);
      
        classroom.getStudents().add(student);
        classroomService.save(classroom);

        
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + student.getRole().name()));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            student.getUsername(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
       
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

       
        return "redirect:/dashboard";
    }
}
