package com.hirwa.classprogram.gallery;

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
import java.util.stream.Collectors;

@Service
public class GalleryService {

    @Autowired
    private GalleryRepository galleryRepository;

    private static final String UPLOAD_DIR = "uploads/gallery/";

    public List<Gallery> findAll() {
        return galleryRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Gallery> getLatestThreeImages() {
        return galleryRepository.findByFileTypeOrderByCreatedAtDesc("IMAGE").stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    public Optional<Gallery> findById(Long id) {
        return galleryRepository.findById(id);
    }

    public Gallery save(Gallery gallery, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.write(uploadPath.resolve(filename), file.getBytes());
            gallery.setFileName(filename);
            
           
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("video/")) {
                gallery.setFileType("VIDEO");
            } else {
                gallery.setFileType("IMAGE");
            }
        }
        
        if (gallery.getId() == null) {
            gallery.setCreatedAt(LocalDateTime.now());
        } else {
            gallery.setUpdatedAt(LocalDateTime.now());
        }
        
        return galleryRepository.save(gallery);
    }

    public void deleteById(Long id) {
        Optional<Gallery> gallery = galleryRepository.findById(id);
        if (gallery.isPresent()) {
        
            try {
                Path filePath = Paths.get(UPLOAD_DIR + gallery.get().getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            galleryRepository.deleteById(id);
        }
    }
}
