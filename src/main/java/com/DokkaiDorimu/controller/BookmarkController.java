package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.repository.ArticleRepository;
import com.DokkaiDorimu.repository.BookmarkRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.DTO.BookmarkedArticleDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.ArticleNotFoundException;
import com.DokkaiDorimu.service.BookmarkService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final BookmarkRepository bookmarkRepository;


    @PostMapping("/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addBookmark(@PathVariable Long articleId) {
        bookmarkService.addBookmark(articleId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long articleId) {
        bookmarkService.removeBookmark(articleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookmarkedArticleDTO>> getBookmarkedArticles() {
        List<BookmarkedArticleDTO> bookmarkedArticles = bookmarkService.getBookmarkedArticles();
        return new ResponseEntity<>(bookmarkedArticles, HttpStatus.OK);
    }
    @GetMapping("/{articleId}/bookmark-status")
    public ResponseEntity<Map<String, Boolean>> getBookmarkStatus(@PathVariable Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));

        boolean bookmarked = bookmarkRepository.findByArticleAndUser(article, currentUser).isPresent();

        Map<String, Boolean> response = new HashMap<>();
        response.put("bookmarked", bookmarked);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}