package com.hirwa.classprogram.gallery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

 
    @GetMapping
    public String listGallery(Model model) {
        try {
            model.addAttribute("galleryList", galleryService.findAll());
        } catch (Exception e) {
            model.addAttribute("galleryList", java.util.Collections.emptyList());
        }
        model.addAttribute("isDS", isDS());
        return "gallery/list";
    }

   
    @GetMapping("/create")
    public String createForm(Model model) {
        if (!isDS()) {
            return "redirect:/";
        }
        model.addAttribute("gallery", new Gallery());
        return "gallery/create";
    }

   
    @PostMapping("/create")
    public String create(@ModelAttribute Gallery gallery,
                        @RequestParam MultipartFile file,
                        RedirectAttributes redirectAttributes) {
        if (!isDS()) {
            return "redirect:/";
        }
        try {
            galleryService.save(gallery, file);
            redirectAttributes.addFlashAttribute("message", "Media uploaded successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/gallery";
    }

    
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isDS()) {
            return "redirect:/";
        }
        galleryService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Media deleted successfully!");
        return "redirect:/gallery";
    }

    private boolean isDS() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_DS"));
    }
}

