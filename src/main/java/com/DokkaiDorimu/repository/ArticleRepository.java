package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.DTO.ArticleProjection;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.ArticleRepositoryCustom;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom {
    List<Article> findByCategory(String category);
    List<Article> findBySubcategory(String subcategory);
    List<Article> findByUserId(Long userId);
    long countByUser(User user);
    List<Article> findByUser(User user);
    Optional<Article> findByIdWithComments(@Param("id") Long id);
    @Query(value = "SELECT * FROM article WHERE MATCH(title, content) AGAINST(:keyword IN NATURAL LANGUAGE MODE)", nativeQuery = true)
    List<Article> searchByTitleAndContent(@Param("keyword") String keyword);
    @Query("SELECT a.id as id, a.title as title, a.content as content, a.category as category, " +
            "a.subcategory as subcategory, a.imageUrl as imageUrl, a.user.username as username, " +
            "a.user.imgUrl as userImgUrl, SIZE(a.comments) as commentCount, SIZE(a.likes) as likeCount, " +
            "a.createdAt as createdAt, SIZE(a.views) as viewCount, SIZE(a.bookmarks) as bookmarkCount " +
            "FROM Article a")
    Page<ArticleProjection> findAllArticlesWithCounts(Pageable pageable);

    @Query("SELECT a.id as id, a.title as title, a.content as content, " +
            "a.category as category, a.subcategory as subcategory, " +
            "a.imageUrl as imageUrl, a.user.username as username, " +
            "a.user.imgUrl as userImgUrl, SIZE(a.comments) as commentCount, " +
            "SIZE(a.likes) as likeCount, a.createdAt as createdAt, " +
            "SIZE(a.views) as viewCount, SIZE(a.bookmarks) as bookmarkCount " +
            "FROM Article a " +
            "ORDER BY a.createdAt DESC")
    List<ArticleProjection> findLatestArticles(Pageable pageable);

    @Query("SELECT a.id as id, a.title as title, a.content as content, " +
            "a.category as category, a.subcategory as subcategory, " +
            "a.imageUrl as imageUrl, a.user.username as username, " +
            "a.user.imgUrl as userImgUrl, SIZE(a.comments) as commentCount, " +
            "SIZE(a.likes) as likeCount, a.createdAt as createdAt, " +
            "SIZE(a.views) as viewCount, SIZE(a.bookmarks) as bookmarkCount " +
            "FROM Article a " +
            "ORDER BY (SIZE(a.likes) + SIZE(a.comments) + SIZE(a.bookmarks)) DESC")
    List<ArticleProjection> findHotArticles(Pageable pageable);
}