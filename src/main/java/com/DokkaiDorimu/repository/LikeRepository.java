package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.Like;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByArticleAndUser(Article article, User user);
    List<Like> findByArticleId(Long articleId);
    void deleteByArticleId(Long articleId);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.article.id = :articleId")
    int countByArticleId(@Param("articleId") Long articleId);
}