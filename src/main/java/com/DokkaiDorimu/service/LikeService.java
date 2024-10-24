package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.LikeDTO;

import java.util.List;

public interface LikeService {
    int likeArticle(Long articleId);
    int removeLike(Long articleId);
    List<LikeDTO> getLikesByArticleId(Long articleId);

}