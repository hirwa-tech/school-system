package com.hirwa.classprogram.classroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;

import java.util.List;

@Controller
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listClassrooms(Authentication auth, Model model) {
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        List<Classroom> classrooms;
        if (user.getRole() == User.Role.TEACHER) {
            classrooms = classroomService.findByTeacher(user);
        } else {
            classrooms = classroomService.findByStudent(user);
        }

        model.addAttribute("classrooms", classrooms);
        model.addAttribute("user", user);
        return "classroom/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("classroom", new Classroom());
        return "classroom/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Classroom classroom, Authentication auth, RedirectAttributes redirectAttributes) {
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();
        classroom.setTeacher(teacher);
        classroomService.save(classroom);
        redirectAttributes.addFlashAttribute("message", "Classroom created successfully!");
        return "redirect:/classroom";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        Classroom classroom = classroomService.findById(id).orElseThrow();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        // Check if user has access
        if (user.getRole() == User.Role.TEACHER && !classroom.getTeacher().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        if (user.getRole() == User.Role.STUDENT && !classroom.getStudents().contains(user)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("classroom", classroom);
        model.addAttribute("user", user);
        model.addAttribute("isTeacher", user.getRole() == User.Role.TEACHER && classroom.getTeacher().getId().equals(user.getId()));
        return "classroom/view";
    }

    @GetMapping("/{id}/join")
    public String joinForm(@PathVariable Long id, Model model) {
        model.addAttribute("classroomId", id);
        return "classroom/join";
    }

    @PostMapping("/{id}/join")
    public String join(@PathVariable Long id, @RequestParam String password, Authentication auth, RedirectAttributes redirectAttributes) {
        Classroom classroom = classroomService.findById(id).orElseThrow();
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();

        if (classroomService.checkPassword(classroom, password)) {
            classroom.getStudents().add(student);
            classroomService.save(classroom);
            redirectAttributes.addFlashAttribute("message", "Joined classroom successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid password!");
        }
        return "redirect:/classroom";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        Classroom classroom = classroomService.findById(id).orElseThrow();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        if (user.getRole() != User.Role.DS && (user.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(user.getId()))) {
            return "redirect:/dashboard";
        }

        model.addAttribute("classroom", classroom);
        return "classroom/edit";
    }


    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Classroom classroom, @RequestParam(required = false) String password, Authentication auth, RedirectAttributes redirectAttributes) {
        Classroom existingClassroom = classroomService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (teacher.getRole() != User.Role.TEACHER || !existingClassroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        existingClassroom.setName(classroom.getName());
        existingClassroom.setDescription(classroom.getDescription());
        
        if (password != null && !password.isEmpty()) {
            existingClassroom.setPassword(password);
        }
        
        classroomService.save(existingClassroom);
        redirectAttributes.addFlashAttribute("message", "Classroom updated successfully!");
        return "redirect:/classroom/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        Classroom classroom = classroomService.findById(id).orElseThrow();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        if (user.getRole() != User.Role.DS && (user.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(user.getId()))) {
            return "redirect:/dashboard";
        }

        classroomService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Classroom deleted successfully!");
        return "redirect:/classroom";
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        return delete(id, auth, redirectAttributes);
    }
}

