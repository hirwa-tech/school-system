package com.hirwa.classprogram.message;

import com.hirwa.classprogram.user.User;
import com.hirwa.classprogram.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard/messages/{id}")
    @PreAuthorize("hasRole('DS')")
    public String view(@PathVariable Long id, Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        Optional<Message> optMsg = messageService.findById(id);
        if (optMsg.isEmpty()) {
            return "redirect:/dashboard/messages";
        }
        model.addAttribute("user", user);
        model.addAttribute("message", optMsg.get());
        return "dashboard/message-view";
    }

    @PostMapping("/dashboard/messages/{id}/delete")
    @PreAuthorize("hasRole('DS')")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        messageService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Message deleted successfully!");
        return "redirect:/dashboard/messages";
    }

    private User getCurrentUser(Authentication auth) {
        if (auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        String username = auth.getName();
        return userService.findByUsername(username).orElseThrow();
    }
}



