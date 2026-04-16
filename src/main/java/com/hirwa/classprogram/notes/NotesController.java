package com.hirwa.classprogram.notes;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import com.hirwa.classprogram.classroom.Classroom;
import com.hirwa.classprogram.classroom.ClassroomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @GetMapping("/classroom/{classroomId}")
    public String listNotes(@PathVariable Long classroomId, Authentication auth, Model model) {
        Classroom classroom = classroomService.findById(classroomId).orElseThrow();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

    
        if (!hasAccess(classroom, user)) {
            return "redirect:/dashboard";
        }

        var notes = noteService.findByClassroomId(classroomId);
        model.addAttribute("notes", notes);
        model.addAttribute("classroom", classroom);
        model.addAttribute("user", user);
        return "notes/list";
    }

    @GetMapping("/create/{classroomId}")
    public String createForm(@PathVariable Long classroomId, Model model) {
        model.addAttribute("note", new Note());
        model.addAttribute("classroomId", classroomId);
        return "notes/create";
    }

    @PostMapping("/create/{classroomId}")
    public String create(@PathVariable Long classroomId, @ModelAttribute Note note, 
                         @RequestParam(required = false) org.springframework.web.multipart.MultipartFile file,
                         Authentication auth, RedirectAttributes redirectAttributes) {
        Classroom classroom = classroomService.findById(classroomId).orElseThrow();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        if (teacher.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

     
        if (file != null && !file.isEmpty()) {
            try {
                note.setFileName(file.getOriginalFilename());
                note.setFileType(file.getContentType());
                note.setFileSize(file.getSize());
                note.setFileData(file.getBytes());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
                return "redirect:/notes/classroom/" + classroomId;
            }
        }

        note.setClassroom(classroom);
        note.setTeacher(teacher);
        noteService.save(note);
        redirectAttributes.addFlashAttribute("message", "Note created successfully!");
        return "redirect:/notes/classroom/" + classroomId;
    }

    @GetMapping("/{id}")
    public String viewNote(@PathVariable Long id, Authentication auth, Model model) {
        Note note = noteService.findById(id).orElseThrow();
        Classroom classroom = note.getClassroom();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

       
        if (!hasAccess(classroom, user)) {
            return "redirect:/dashboard";
        }

        boolean isTeacher = user.getRole() == User.Role.TEACHER && classroom.getTeacher().getId().equals(user.getId());
        
        model.addAttribute("note", note);
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("user", user);
        return "notes/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        Note note = noteService.findById(id).orElseThrow();
        Classroom classroom = note.getClassroom();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        
        if (teacher.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("note", note);
        model.addAttribute("classroomId", classroom.getId());
        return "notes/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Note note, 
                      @RequestParam(required = false) org.springframework.web.multipart.MultipartFile file,
                      Authentication auth, RedirectAttributes redirectAttributes) {
        Note existingNote = noteService.findById(id).orElseThrow();
        Classroom classroom = existingNote.getClassroom();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

        
        if (teacher.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        existingNote.setTitle(note.getTitle());
        existingNote.setContent(note.getContent());
        
        
        if (file != null && !file.isEmpty()) {
            try {
                existingNote.setFileName(file.getOriginalFilename());
                existingNote.setFileType(file.getContentType());
                existingNote.setFileSize(file.getSize());
                existingNote.setFileData(file.getBytes());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
            }
        }
        
        noteService.save(existingNote);
        
        redirectAttributes.addFlashAttribute("message", "Note updated successfully!");
        return "redirect:/notes/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        Note note = noteService.findById(id).orElseThrow();
        Classroom classroom = note.getClassroom();
        String username = auth.getName();
        User teacher = userService.findByUsername(username).orElseThrow();

      
        if (teacher.getRole() != User.Role.TEACHER || !classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/dashboard";
        }

        Long classroomId = classroom.getId();
        noteService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Note deleted successfully!");
        return "redirect:/notes/classroom/" + classroomId;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, Authentication auth) throws Exception {
        Note note = noteService.findById(id).orElseThrow();
        Classroom classroom = note.getClassroom();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();

      
        if (!hasAccess(classroom, user)) {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).build();
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(note.getFileType()))
                .header("Content-Disposition", "attachment; filename=\"" + note.getFileName() + "\"")
                .body(note.getFileData());
    }

    private boolean hasAccess(Classroom classroom, User user) {
        if (user.getRole() == User.Role.TEACHER) {
            return classroom.getTeacher().getId().equals(user.getId());
        } else {
            return classroom.getStudents().contains(user);
        }
    }
}
