package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByArticleId(Long articleId);
    void deleteByArticleId(Long articleId);
}
