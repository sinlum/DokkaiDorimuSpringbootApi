package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    CommentDTO addComment(Long articleId, Long parentCommentId, String content);
    List<CommentDTO> getCommentsByArticleId(Long articleId);
//    void deleteComment(Long commentId);
    public void deleteComment(Long articleId, Long commentId);
    public boolean isCommentOwner(Long commentId, String userEmail);
    Page<CommentDTO> getCommentsByArticle(Long articleId, Pageable pageable);
    Page<CommentDTO> getRepliesByParentComment(Long parentId, Pageable pageable);

}