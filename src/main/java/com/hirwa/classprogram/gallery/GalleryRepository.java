package com.hirwa.classprogram.gallery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
    List<Gallery> findAllByOrderByCreatedAtDesc();

    List<Gallery> findByFileTypeOrderByCreatedAtDesc(String fileType);
}
