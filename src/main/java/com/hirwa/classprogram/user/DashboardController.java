package com.hirwa.classprogram.user;
import com.hirwa.classprogram.classroom.Classroom;
import com.hirwa.classprogram.classroom.ClassroomService;
import com.hirwa.classprogram.message.MessageService;
import com.hirwa.classprogram.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user;
        
        
        if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
        } else {
            
            String username = auth.getName();
            user = userService.findByUsername(username).orElse(null);
        }
        
       
        if (user == null) {
            return "redirect:/login";
        }

        List<Classroom> classrooms;
       
        if (user.getRole() == User.Role.DS) {
            classrooms = List.of();
        } else if (user.getRole() == User.Role.TEACHER) {
            classrooms = classroomService.findByTeacher(user);
        } else {
            classrooms = classroomService.findByStudent(user);
        }

        model.addAttribute("user", user);
        model.addAttribute("classrooms", classrooms);
        return "dashboard";
    }

@GetMapping("/dashboard/messages")
    public String messages(Authentication auth, Model model) {
        User user;
        if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
        } else {
            String username = auth.getName();
            user = userService.findByUsername(username).orElse(null);
        }
        
        if (user == null || user.getRole() != User.Role.DS) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("messages", messageService.findAll());
        return "dashboard/messages";
    }

@GetMapping("/dashboard/teachers")
    public String teachers(Authentication auth, Model model) {
        User user;
        if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
        } else {
            String username = auth.getName();
            user = userService.findByUsername(username).orElse(null);
        }
        
        if (user == null || user.getRole() != User.Role.DS) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("teachers", userService.findTeachers());
        return "dashboard/teachers";
    }


    @GetMapping("/dashboard/classrooms")
    public String classrooms(Authentication auth, Model model) {
        User user;
        if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
        } else {
            String username = auth.getName();
            user = userService.findByUsername(username).orElse(null);
        }
        
        if (user == null || user.getRole() != User.Role.DS) {
            return "redirect:/dashboard";
        }

        List<Classroom> classrooms = classroomService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("classrooms", classrooms);
        return "dashboard/classrooms";
    }
}


