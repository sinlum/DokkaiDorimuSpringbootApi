package com.DokkaiDorimu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "article")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "comments", "likes", "views", "quizQuestions"})
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Size(max = 100, message = "Title should not exceed 100 characters")
    private String title;

    @NotBlank(message = "Content is mandatory")
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @NotBlank(message = "Category is mandatory")
    private String category;

    @NotBlank(message = "Subcategory is mandatory")
    private String subcategory;

    @NotBlank(message = "Image is mandatory")
    private String imageUrl;

    @NotBlank(message = "audio is mandatory")
    private String audioUrl;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("article")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<View> views = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "article")
    @JsonIgnore
    private List<QuizScore> quizScores = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private int bookmarkCount = 0;

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    public int getViewCount() {
        return views != null ? views.size() : 0;
    }
    public int getBookmarkCount() {
        return bookmarks != null ? bookmarks.size() : 0;
    }
}