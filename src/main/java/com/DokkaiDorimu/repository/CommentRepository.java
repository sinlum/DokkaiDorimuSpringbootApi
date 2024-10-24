package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleId(Long articleId);
    void deleteByArticleId(Long articleId);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.article.id = :articleId")
    int countByArticleId(@Param("articleId") Long articleId);
    Optional<Comment> findByIdAndArticleId(Long id, Long articleId);
    Page<Comment> findByArticleIdAndParentCommentIsNull(Long articleId, Pageable pageable);
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId")
    Page<Comment> findByParentCommentId(@Param("parentId") Long parentId, Pageable pageable);
}