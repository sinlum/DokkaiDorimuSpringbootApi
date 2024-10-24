package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkedArticleDTO {
    private Long articleId;
    private String title;
    private String content;
    private String category;
    private String subcategory;
    private String imageUrl;
    private String username;
    private String authorImg;
}