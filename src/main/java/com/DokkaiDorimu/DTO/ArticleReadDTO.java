package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ArticleReadDTO {
    private Long id;
    private String title;
    private String category;
    private String subcategory;
    private LocalDateTime readAt;
}
