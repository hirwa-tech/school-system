package com.hirwa.classprogram;

import com.hirwa.classprogram.gallery.GalleryService;
import com.hirwa.classprogram.message.Message;
import com.hirwa.classprogram.message.MessageService;
import com.hirwa.classprogram.news.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class PublicController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private GalleryService galleryService;

@ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null && 
               SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("newsList", newsService.getLatestThreeNews());
        model.addAttribute("galleryList", galleryService.getLatestThreeImages());
        return "index";
    }

    @GetMapping("/index")
    public String home(Model model) {
        model.addAttribute("newsList", newsService.getLatestThreeNews());
        model.addAttribute("galleryList", galleryService.getLatestThreeImages());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/admission")
    public String admission() {
        return "admission";
    }

    @GetMapping("/academic")
    public String academic() {
        return "academic";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        Message msg = new Message();
        msg.setType(Message.Type.CONTACT);
        msg.setName(name);
        msg.setPhone(phone);
        msg.setEmail(email);
        msg.setSubject(subject);
        msg.setMessage(message);
        messageService.save(msg);

        redirectAttributes.addFlashAttribute("success", "Thank you for your message! We will get back to you soon.");
        return "redirect:/contact";
    }

    @PostMapping("/admission")
    public String submitAdmission(
            @RequestParam String fullName,
            @RequestParam String classApplyingFor,
            @RequestParam(required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {
        Message msg = new Message();
        msg.setType(Message.Type.ADMISSION);
        msg.setName(fullName);
        msg.setClassLevel(classApplyingFor);
        msg.setEmail(""); 

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/messages/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            fileUrl = fileName;
        }
        msg.setFileUrl(fileUrl);

        messageService.save(msg);
        redirectAttributes.addFlashAttribute("success", "Application submitted successfully! We will contact you soon.");
        return "redirect:/admission";
    }
}

