package com.DokkaiDorimu.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String subcategory;
    private String imageUrl;
    private String username;
    private Long userId;
    private String status;
    private String authorImageUrl;
    private String audioUrl;
//    private List<CommentDTO> comments;
    private List<QuizQuestionDTO> quizQuestions;
    private int commentCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private int viewCount;
    private int bookmarkCount;
}