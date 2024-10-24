package com.DokkaiDorimu.service;

import com.DokkaiDorimu.exception.ArticleNotFoundException;
import com.DokkaiDorimu.exception.UnauthorizedException;
import com.DokkaiDorimu.repository.ArticleRepository;
import com.DokkaiDorimu.repository.LikeRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.DTO.LikeDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.Like;
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
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public int likeArticle(Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));

        Optional<Like> existingLike = likeRepository.findByArticleAndUser(article, currentUser);

        if (existingLike.isPresent()) {
            throw new UnauthorizedException("You have already liked this article");
        }

        Like like = new Like();
        like.setUser(currentUser);
        like.setArticle(article);
        likeRepository.save(like);

        return article.getLikeCount();
    }

    @Override
    public int removeLike(Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));

        Optional<Like> existingLike = likeRepository.findByArticleAndUser(article, currentUser);

        if (existingLike.isEmpty()) {
            throw new UnauthorizedException("You have not liked this article");
        }

        likeRepository.delete(existingLike.get());

        return article.getLikeCount();
    }

    @Override
    public List<LikeDTO> getLikesByArticleId(Long articleId) {
        return likeRepository.findByArticleId(articleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private LikeDTO convertToDTO(Like like) {
        return new LikeDTO(
                like.getId(),
                like.getUser().getUsername(),
                like.getArticle().getId()
        );
    }
}