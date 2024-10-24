package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContributionDTO {
    private Long articleId;
    private String title;
    private int likeCount;
    private int commentCount;
    private int bookmarkCount;
    private int viewCount;
    private String category;
    private String imageUrl;
    private String content;
    private LocalDateTime createdAt;
}
