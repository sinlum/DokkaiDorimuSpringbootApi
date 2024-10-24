package com.DokkaiDorimu.DTO;

import java.time.LocalDateTime;

public interface ArticleProjection {
    Long getId();
    String getTitle();
    String getContent();
    String getCategory();
    String getSubcategory();
    String getImageUrl();
    String getUsername();
    String getUserImgUrl();
    int getCommentCount();
    int getLikeCount();
    LocalDateTime getCreatedAt();
    int getViewCount();
    int getBookmarkCount();
}
