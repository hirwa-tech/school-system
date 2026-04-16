package com.hirwa.classprogram.news;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "news")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String documentName;
    private String documentPath; 
    private Long documentSize; 
    
    private String imageName; 
    private String imagePath; 

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "news_likes", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "username")
    private List<String> likes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "news_comments", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();
}

