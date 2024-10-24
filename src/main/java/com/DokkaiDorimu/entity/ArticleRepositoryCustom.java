package com.DokkaiDorimu.entity;

import java.util.Optional;

public interface ArticleRepositoryCustom {
    Optional<Article> findByIdWithComments(Long id);
}
