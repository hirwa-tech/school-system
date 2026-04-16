package com.hirwa.classprogram.homework;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import com.hirwa.classprogram.classroom.Classroom;
import com.hirwa.classprogram.classroom.ClassroomService;
import com.hirwa.classprogram.common.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/homework")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/classroom/{classroomId}")
    public String listHomeworks(@PathVariable Long classroomId, Authentication auth, Model model) {
        Classroom classroom = classroomService.findById(classroomId).orElseThrow();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        if (!hasAccess(classroom, user)) {
            return "redirect:/dashboard";
        }

        var homeworks = homeworkService.findByClassroomId(classroomId);
        model.addAttribute("homeworks", homeworks);
        model.addAttribute("classroom", classroom);
        model.addAttribute("user", user);
        model.addAttribute("isTeacher", user.getRole() == User.Role.TEACHER && classroom.getTeacher().getId().equals(user.getId()));
        return "homework/list";
    }

    @GetMapping("/create/{classroomId}")
    public String createForm(@PathVariable Long classroomId, Model model) {
        model.addAttribute("homework", new Homework());
        model.addAttribute("classroomId", classroomId);
        return "homework/create";
    }

    @PostMapping("/create/{classroomId}")
    public String create(@PathVariable Long classroomId, @ModelAttribute Homework homework, 
                         @RequestParam(required = false) MultipartFile file,
                         Authentication auth, RedirectAttributes redirectAttributes) {
        Classroom classroom = classroomService.findById(classroomId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (teacher.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

  
        if (file != null && !file.isEmpty()) {
            homework.setFileName(file.getOriginalFilename());
            homework.setFileType(file.getContentType());
            homework.setFileSize(file.getSize());
            try {
                homework.setFileData(file.getBytes());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file");
            }
        }

        homework.setClassroom(classroom);
        homework.setTeacher(teacher);
        homeworkService.save(homework);
        redirectAttributes.addFlashAttribute("message", "Homework created successfully!");
        return "redirect:/homework/classroom/" + classroomId;
    }

    @GetMapping("/{id}/submit")
    public String submitForm(@PathVariable Long id, Model model) {
        model.addAttribute("homeworkId", id);
        return "homework/submit";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, @RequestParam String submissionText, @RequestParam(required = false) MultipartFile file, Authentication auth, RedirectAttributes redirectAttributes) {
        Homework homework = homeworkService.findById(id).orElseThrow();
        String username = auth.getName();
        User student = userService.findByUsername(username).orElseThrow();

        
        if (!homework.getClassroom().getStudents().contains(student)) {
            return "redirect:/dashboard";
        }

        
        String subject = "Homework Submission";
        String body = String.format("Student: %s %s\nClass: %s\nHomework: %s\n\nSubmission:\n%s",
                student.getFirstName(), student.getLastName(),
                homework.getClassroom().getName(),
                homework.getTitle(),
                submissionText);

        try {
            emailService.sendHomeworkSubmission(homework.getTeacher().getEmail(), subject, body, file);
            redirectAttributes.addFlashAttribute("message", "Homework submitted successfully!");
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit homework: " + e.getMessage());
        }

        return "redirect:/homework/classroom/" + homework.getClassroom().getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        Homework homework = homeworkService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

     
        if (teacher.getRole() != User.Role.TEACHER || !homework.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("homework", homework);
        model.addAttribute("classroomId", homework.getClassroom().getId());
        return "homework/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Homework homework, 
                       @RequestParam(required = false) MultipartFile file,
                       Authentication auth, RedirectAttributes redirectAttributes) {
        Homework existingHomework = homeworkService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

      
        if (teacher.getRole() != User.Role.TEACHER || !existingHomework.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        existingHomework.setTitle(homework.getTitle());
        existingHomework.setDescription(homework.getDescription());
        existingHomework.setDueDate(homework.getDueDate());

        if (file != null && !file.isEmpty()) {
            existingHomework.setFileName(file.getOriginalFilename());
            existingHomework.setFileType(file.getContentType());
            existingHomework.setFileSize(file.getSize());
            try {
                existingHomework.setFileData(file.getBytes());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file");
            }
        }
        
        homeworkService.save(existingHomework);
        
        redirectAttributes.addFlashAttribute("message", "Homework updated successfully!");
        return "redirect:/homework/classroom/" + existingHomework.getClassroom().getId();
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        Homework homework = homeworkService.findById(id).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (teacher.getRole() != User.Role.TEACHER || !homework.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        Long classroomId = homework.getClassroom().getId();
        homeworkService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Homework deleted successfully!");
        return "redirect:/homework/classroom/" + classroomId;
    }

    @GetMapping("/{id}/download")
    public String download(@PathVariable Long id, Authentication auth, HttpServletResponse response) {
        Homework homework = homeworkService.findById(id).orElseThrow();
        Classroom classroom = homework.getClassroom();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

        if (!hasAccess(classroom, user)) {
            return "redirect:/dashboard";
        }
        
        if (homework.getFileData() != null) {
            try {
                response.setContentType(homework.getFileType());
                response.setHeader("Content-Disposition", "attachment; filename=\"" + homework.getFileName() + "\"");
                response.getOutputStream().write(homework.getFileData());
                response.getOutputStream().flush();
            } catch (Exception e) {
           
            }
        }
        return null;
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        Homework homework = homeworkService.findById(id).orElseThrow();
        Classroom classroom = homework.getClassroom();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

      
        if (!hasAccess(classroom, user)) {
            return "redirect:/dashboard";
        }

        boolean isTeacher = user.getRole() == User.Role.TEACHER && classroom.getTeacher().getId().equals(user.getId());
        
        model.addAttribute("homework", homework);
        model.addAttribute("user", user);
        model.addAttribute("isTeacher", isTeacher);
        return "homework/view";
    }

    private boolean hasAccess(Classroom classroom, User user) {
        if (user.getRole() == User.Role.TEACHER) {
            return classroom.getTeacher().getId().equals(user.getId());
        } else {
            return classroom.getStudents().contains(user);
        }
    }
}