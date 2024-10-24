package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.Bookmark;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByArticleAndUser(Article article, User user);
    List<Bookmark> findByUser(User user);
    void deleteByArticleId(Long articleId);
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.article.id = :articleId")
    int countByArticleId(@Param("articleId") Long articleId);
}