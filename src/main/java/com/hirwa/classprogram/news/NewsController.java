package com.hirwa.classprogram.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

   
    @GetMapping
    public String listNews(Model model) {
        try {
            model.addAttribute("newsList", newsService.findAll());
        } catch (Exception e) {
            model.addAttribute("newsList", java.util.Collections.emptyList());
        }
        model.addAttribute("isDS", isDS());
        return "news/list";
    }

    
    @GetMapping("/{id}")
    public String viewNews(@PathVariable Long id, Model model) {
        Optional<News> news = newsService.findById(id);
        if (news.isPresent()) {
            model.addAttribute("news", news.get());
            model.addAttribute("isDS", isDS());
            return "news/view";
        }
        return "redirect:/news";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!isDS()) {
            return "redirect:/";
        }
        model.addAttribute("news", new News());
        return "news/create";
    }


    @PostMapping("/create")
    public String create(@ModelAttribute News news,
                         @RequestParam(required = false) MultipartFile document,
                         @RequestParam(required = false) MultipartFile image,
                         RedirectAttributes redirectAttributes) {
        if (!isDS()) {
            return "redirect:/";
        }
        try {
            newsService.save(news, document, image);
            redirectAttributes.addFlashAttribute("message", "News published successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/news";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!isDS()) {
            return "redirect:/";
        }
        Optional<News> news = newsService.findById(id);
        if (news.isPresent()) {
            model.addAttribute("news", news.get());
            return "news/edit";
        }
        return "redirect:/news";
    }

  
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @ModelAttribute News news,
                       @RequestParam(required = false) MultipartFile document,
                       @RequestParam(required = false) MultipartFile image,
                       RedirectAttributes redirectAttributes) {
        if (!isDS()) {
            return "redirect:/";
        }
        Optional<News> existingNews = newsService.findById(id);
        if (existingNews.isPresent()) {
            news.setId(id);
            news.setLikes(existingNews.get().getLikes());
            news.setComments(existingNews.get().getComments());
            news.setCreatedAt(existingNews.get().getCreatedAt());
          
            if ((image == null || image.isEmpty()) && existingNews.get().getImagePath() != null) {
                news.setImageName(existingNews.get().getImageName());
                news.setImagePath(existingNews.get().getImagePath());
            }
            try {
                newsService.save(news, document, image);
                redirectAttributes.addFlashAttribute("message", "News updated successfully!");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
            }
        }
        return "redirect:/news/" + id;
    }

   
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isDS()) {
            return "redirect:/";
        }
        newsService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "News deleted successfully!");
        return "redirect:/news";
    }

   
    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, @RequestParam String username, RedirectAttributes redirectAttributes) {
        newsService.addLike(id, username);
        return "redirect:/news/" + id;
    }

    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id, @RequestParam String comment, RedirectAttributes redirectAttributes) {
        if (comment != null && !comment.trim().isEmpty()) {
            newsService.addComment(id, comment);
        }
        return "redirect:/news/" + id;
    }

    
    @GetMapping("/{id}/download")
    public org.springframework.http.ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Optional<News> newsOpt = newsService.findById(id);
        if (newsOpt.isPresent()) {
            News news = newsOpt.get();
            if (news.getDocumentPath() != null) {
                try {
                    Path filePath = Paths.get("uploads/news/" + news.getDocumentPath());
                    Resource resource = new UrlResource(filePath.toUri());
                    if (resource.exists()) {
                        return org.springframework.http.ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + news.getDocumentName() + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(resource);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return org.springframework.http.ResponseEntity.notFound().build();
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

