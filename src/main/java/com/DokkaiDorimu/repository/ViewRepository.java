package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ViewRepository extends JpaRepository<View,Long> {
    boolean existsByArticleAndViewerIdentifierAndViewedAtAfter(Article article, String viewerIdentifier, LocalDateTime cutoffTime);
}
