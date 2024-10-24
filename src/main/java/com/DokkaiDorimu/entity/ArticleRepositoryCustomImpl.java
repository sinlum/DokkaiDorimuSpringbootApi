package com.DokkaiDorimu.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Article> findByIdWithComments(Long id) {
        TypedQuery<Article> query = entityManager.createQuery(
                "SELECT a FROM Article a LEFT JOIN FETCH a.comments WHERE a.id = :id", Article.class);
        query.setParameter("id", id);
        return Optional.ofNullable(query.getSingleResult());
    }
}
