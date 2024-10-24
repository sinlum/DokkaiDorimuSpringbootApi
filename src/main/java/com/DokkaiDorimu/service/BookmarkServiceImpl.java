package com.DokkaiDorimu.service;

import com.DokkaiDorimu.exception.ArticleNotFoundException;
import com.DokkaiDorimu.exception.UnauthorizedException;
import com.DokkaiDorimu.repository.ArticleRepository;
import com.DokkaiDorimu.repository.BookmarkRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.DTO.BookmarkedArticleDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.Bookmark;
import com.DokkaiDorimu.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public void addBookmark(Long articleId) {
        User currentUser = getCurrentUser();
        Article article = getArticleById(articleId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByArticleAndUser(article, currentUser);
        if (existingBookmark.isPresent()) {
            throw new UnauthorizedException("You have already bookmarked this article");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(currentUser);
        bookmark.setArticle(article);
        bookmarkRepository.save(bookmark);
    }

    @Override
    public void removeBookmark(Long articleId) {
        User currentUser = getCurrentUser();
        Article article = getArticleById(articleId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByArticleAndUser(article, currentUser);
        if (existingBookmark.isEmpty()) {
            throw new UnauthorizedException("You have not bookmarked this article");
        }

        bookmarkRepository.delete(existingBookmark.get());
    }

    public List<BookmarkedArticleDTO> getBookmarkedArticles() {
        User currentUser = getCurrentUser();
        List<Bookmark> bookmarks = bookmarkRepository.findByUser(currentUser);

        return bookmarks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));
    }


    private BookmarkedArticleDTO convertToDTO(Bookmark bookmark) {
        Article article = bookmark.getArticle();
        return new BookmarkedArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getCategory(),
                article.getSubcategory(),
                article.getImageUrl(),
                article.getUser().getUsername(),
                article.getUser().getImgUrl()
        );
    }
}