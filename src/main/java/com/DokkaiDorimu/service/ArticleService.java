package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.ArticleDTO;
import com.DokkaiDorimu.DTO.CommentDTO;
import com.DokkaiDorimu.DTO.DashboardArticlesDTO;
import com.DokkaiDorimu.DTO.QuizQuestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {
    Page<ArticleDTO> getAllArticles(int page, int size);
    ArticleDTO getArticleById(Long id);
    ArticleDTO saveArticle(ArticleDTO articleDTO, MultipartFile image, MultipartFile audio, List<QuizQuestionDTO> quizQuestions);
    void deleteArticle(Long id);
    List<ArticleDTO> getArticlesByCategory(String category);
    List<ArticleDTO> getArticlesBySubcategory(String subcategory);
    ArticleDTO updateArticle(Long id, ArticleDTO updatedArticleDTO, MultipartFile image, MultipartFile audio, List<QuizQuestionDTO> quizQuestions);
    List<ArticleDTO> searchByTitleAndContent(String keyword);
    int getLikeCount(Long articleId);
    List<CommentDTO> getComments(Long articleId);
    ArticleDTO getArticleById(Long id, String viewerIdentifier);
    int getArticleViewCount(Long articleId);
    DashboardArticlesDTO getDashboardArticles();
}
