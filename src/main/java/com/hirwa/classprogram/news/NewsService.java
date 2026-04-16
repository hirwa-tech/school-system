package com.hirwa.classprogram.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    private static final String UPLOAD_DIR = "uploads/news/";

    public List<News> findAll() {
        return newsRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<News> getLatestThreeNews() {
        return newsRepository.findTop3ByOrderByCreatedAtDesc();
    }

    public Optional<News> findById(Long id) {
        return newsRepository.findById(id);
    }

    public News save(News news, MultipartFile document, MultipartFile image) throws IOException {
        
        if (document != null && !document.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + document.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.write(uploadPath.resolve(filename), document.getBytes());
            news.setDocumentName(document.getOriginalFilename());
            news.setDocumentPath(filename);
            news.setDocumentSize(document.getSize());
        }
        
       
        if (image != null && !image.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.write(uploadPath.resolve(filename), image.getBytes());
            news.setImageName(image.getOriginalFilename());
            news.setImagePath(filename);
        }
        
        if (news.getId() == null) {
            news.setCreatedAt(LocalDateTime.now());
        } else {
            news.setUpdatedAt(LocalDateTime.now());
        }
        
        return newsRepository.save(news);
    }

    public void deleteById(Long id) {
        Optional<News> news = newsRepository.findById(id);
        if (news.isPresent()) {
         
            if (news.get().getDocumentPath() != null) {
                try {
                    Path filePath = Paths.get(UPLOAD_DIR + news.get().getDocumentPath());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            newsRepository.deleteById(id);
        }
    }

    public News addLike(Long newsId, String username) {
        Optional<News> newsOpt = newsRepository.findById(newsId);
        if (newsOpt.isPresent()) {
            News news = newsOpt.get();
            if (!news.getLikes().contains(username)) {
                news.getLikes().add(username);
                return newsRepository.save(news);
            }
        }
        return newsOpt.orElse(null);
    }

    public News addComment(Long newsId, String comment) {
        Optional<News> newsOpt = newsRepository.findById(newsId);
        if (newsOpt.isPresent()) {
            News news = newsOpt.get();
            news.getComments().add(comment);
            return newsRepository.save(news);
        }
        return newsOpt.orElse(null);
    }
}
