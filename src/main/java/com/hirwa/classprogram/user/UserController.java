package com.hirwa.classprogram.user;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('DS')")
    public String listTeachers(Model model) {
        model.addAttribute("teachers", userService.findAll().stream()
            .filter(u -> u.getRole() == User.Role.TEACHER)
            .toList());
        return "redirect:/dashboard/teachers"; 
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('DS')")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        User currentUser = getCurrentUser(auth);
        User teacher = userService.findById(id).orElseThrow();
        if (teacher.getRole() != User.Role.TEACHER || teacher.getId().equals(currentUser.getId())) {
            return "redirect:/dashboard/teachers";
        }
        model.addAttribute("teacher", teacher);
        model.addAttribute("user", currentUser);
        return "users/edit";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('DS')")
    public String update(@PathVariable Long id, @ModelAttribute User userData, Authentication auth, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(auth);
        User teacher = userService.findById(id).orElseThrow();
        if (teacher.getRole() != User.Role.TEACHER || teacher.getId().equals(currentUser.getId())) {
            return "redirect:/dashboard/teachers";
        }

        teacher.setUsername(userData.getUsername());
        teacher.setEmail(userData.getEmail());
        teacher.setFirstName(userData.getFirstName());
        teacher.setLastName(userData.getLastName());
        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(userData.getPassword()));
        }
        userService.save(teacher);
        redirectAttributes.addFlashAttribute("success", "Teacher updated successfully!");
        return "redirect:/dashboard/teachers";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DS')")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(auth);
        User teacher = userService.findById(id).orElseThrow();
        if (teacher.getRole() != User.Role.TEACHER || teacher.getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete this user.");
            return "redirect:/dashboard/teachers";
        }
        userService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Teacher deleted successfully!");
        return "redirect:/dashboard/teachers";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('DS')")
    public String deletePost(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        return delete(id, auth, redirectAttributes);
    }

    private User getCurrentUser(Authentication auth) {
        if (auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return userService.findByUsername(auth.getName()).orElseThrow();
    }
}

