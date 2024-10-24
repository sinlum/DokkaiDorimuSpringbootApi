package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.DTO.DashboardArticlesDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.DokkaiDorimu.DTO.QuizQuestionDTO;
import com.DokkaiDorimu.repository.ArticleRepository;
import com.DokkaiDorimu.repository.LikeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.DokkaiDorimu.DTO.ArticleDTO;
import com.DokkaiDorimu.DTO.CommentDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.ArticleNotFoundException;
import com.DokkaiDorimu.exception.UnauthorizedException;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.service.ArticleService;
import com.DokkaiDorimu.service.LikeService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final LikeService likeService;
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private final LikeRepository likeRepository;
    private UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<Page<ArticleDTO>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleDTO> articleDTOs = articleService.getAllArticles(page, size);
        return ResponseEntity.ok(articleDTOs);
    }

    @PostMapping
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<ArticleDTO> createArticle(
            @RequestParam("article") String articleData,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "audio", required = false) MultipartFile audio,
            @RequestParam("quizQuestions") String quizQuestionsData) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArticleDTO articleDTO = objectMapper.readValue(articleData, ArticleDTO.class);
            List<QuizQuestionDTO> quizQuestions = objectMapper.readValue(quizQuestionsData,
                    new TypeReference<List<QuizQuestionDTO>>(){});

            ArticleDTO createdArticle = articleService.saveArticle(articleDTO, image, audio, quizQuestions);
            return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ArticleDTO>> getArticlesByCategory(@PathVariable String category) {
        List<ArticleDTO> articlesByCategory = articleService.getArticlesByCategory(category);
        return new ResponseEntity<>(articlesByCategory, HttpStatus.OK);
    }

    @GetMapping("/subcategory/{subcategory}")
    public ResponseEntity<List<ArticleDTO>> getArticlesBySubcategory(@PathVariable String subcategory) {
        List<ArticleDTO> articlesBySubcategory = articleService.getArticlesBySubcategory(subcategory);
        return new ResponseEntity<>(articlesBySubcategory, HttpStatus.OK);
    }




    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return ResponseEntity.ok().build();
        } catch (ArticleNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<ArticleDTO> updateArticle(
            @PathVariable Long id,
            @RequestPart("article") String articleJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestPart(value = "quizQuestions", required = false) String quizQuestionsJson
    ) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Add this if you're using Java 8 date/time types

        ArticleDTO articleDTO = objectMapper.readValue(articleJson, ArticleDTO.class);

        List<QuizQuestionDTO> quizQuestions = new ArrayList<>();
        if (quizQuestionsJson != null && !quizQuestionsJson.isEmpty()) {
            quizQuestions = objectMapper.readValue(quizQuestionsJson, new TypeReference<List<QuizQuestionDTO>>() {});
        }

        ArticleDTO updatedArticle = articleService.updateArticle(id, articleDTO, image, audio, quizQuestions);
        return ResponseEntity.ok(updatedArticle);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArticleDTO>> searchByTitleAndContent(@RequestParam String query) {
        try {
            List<ArticleDTO> articles = articleService.searchByTitleAndContent(query);
            return new ResponseEntity<>(articles, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred during search: {}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<Integer> likeArticle(@PathVariable Long id) {
        try {
            int likeCount = likeService.likeArticle(id);
            return new ResponseEntity<>(likeCount, HttpStatus.OK);
        } catch (ArticleNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/like/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> removeLike(@PathVariable Long id) {
        try {
            int likeCount = likeService.removeLike(id);
            return new ResponseEntity<>(likeCount, HttpStatus.OK);
        } catch (ArticleNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (UnauthorizedException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/likes/{id}")
    public ResponseEntity<Integer> getLikeCount(@PathVariable Long id) {
        try {
            int likeCount = articleService.getLikeCount(id);
            return new ResponseEntity<>(likeCount, HttpStatus.OK);
        } catch (ArticleNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/{id}/like-status")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));

        boolean liked = likeRepository.findByArticleAndUser(article, currentUser).isPresent();

        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", liked);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long id) {
        try {
            List<CommentDTO> comments = articleService.getComments(id);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (ArticleNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticle(@PathVariable Long id, HttpServletRequest request) {
        String viewerIdentifier = request.getRemoteAddr(); // Using IP address as identifier
        ArticleDTO article = articleService.getArticleById(id, viewerIdentifier);
        return ResponseEntity.ok(article);
    }

    @GetMapping("/{id}/view-count")
    public ResponseEntity<Integer> getArticleViewCount(@PathVariable Long id) {
        int viewCount = articleService.getArticleViewCount(id);
        return ResponseEntity.ok(viewCount);
    }
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardArticlesDTO> getDashboardArticles() {
        return ResponseEntity.ok(articleService.getDashboardArticles());
    }
}