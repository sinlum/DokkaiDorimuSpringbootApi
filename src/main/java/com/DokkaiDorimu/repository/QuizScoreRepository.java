package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.DTO.CategoryScoreDTO;
import com.DokkaiDorimu.DTO.LeaderboardEntryDTO;
import com.DokkaiDorimu.DTO.UserTotalScoreDTO;
import com.DokkaiDorimu.entity.QuizScore;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizScoreRepository extends JpaRepository<QuizScore, Long> {
    List<QuizScore> findByUser(User user);
    List<QuizScore> findByUserAndCategory(User user, String category);

    @Query("SELECT new com.DokkaiDorimu.DTO.CategoryScoreDTO(qs.category, AVG(qs.score)) " +
            "FROM QuizScore qs " +
            "LEFT JOIN qs.article a " +  // Add LEFT JOIN
            "WHERE qs.user = :user " +
            "GROUP BY qs.category")
    List<CategoryScoreDTO> getAverageScoresByCategory(@Param("user") User user);

    @Query("SELECT AVG(qs.score) " +
            "FROM QuizScore qs " +
            "LEFT JOIN qs.article a " +  // Add LEFT JOIN
            "WHERE qs.user = :user")
    Double getOverallAverageScore(@Param("user") User user);

    // For getting total quiz count
    @Query("SELECT COUNT(qs) " +
            "FROM QuizScore qs " +
            "LEFT JOIN qs.article a " +  // Add LEFT JOIN
            "WHERE qs.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT qs FROM QuizScore qs " +
            "JOIN FETCH qs.article " +  // Changed to INNER JOIN
            "WHERE qs.user.id = :userId " +
            "AND qs.articleRead = true " +
            "ORDER BY COALESCE(qs.completedAt, CURRENT_TIMESTAMP) DESC")
    List<QuizScore> findByUserIdWithArticles(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT qs.article.id) " +
            "FROM QuizScore qs " +
            "WHERE qs.user.id = :userId " +
            "AND qs.articleRead = true")
    long countUniqueArticlesReadByUser(@Param("userId") Long userId);

    @Query("SELECT qs.category, COUNT(DISTINCT qs.article.id) " +
            "FROM QuizScore qs " +
            "WHERE qs.user.id = :userId " +
            "AND qs.articleRead = true " +
            "GROUP BY qs.category")
    List<Object[]> getArticleCountByCategory(@Param("userId") Long userId);

    @Query("SELECT DISTINCT qs.article.id, qs.article.title, qs.article.category, " +
            "COALESCE(qs.completedAt, qs.article.createdAt) " +
            "FROM QuizScore qs " +
            "JOIN qs.article " +
            "WHERE qs.user.id = :userId " +
            "AND qs.articleRead = true " +
            "ORDER BY COALESCE(qs.completedAt, qs.article.createdAt) DESC")
    List<Object[]> findRecentlyReadArticles(@Param("userId") Long userId);

    @Query("SELECT new com.DokkaiDorimu.DTO.LeaderboardEntryDTO(" +
            "qs.user.id, " +
            "qs.user.username, " +
            "qs.user.imgUrl, " +
            "qs.category, " +
            "AVG(qs.score), " +
            "COUNT(qs), " +
            "MAX(qs.score), " +
            "SUM(qs.score * 1.0)) " +
            "FROM QuizScore qs " +
            "WHERE qs.category = :category " +
            "AND qs.completedAt >= :startDate " +
            "GROUP BY qs.user.id, qs.user.username, qs.category " +
            "ORDER BY AVG(qs.score) DESC, COUNT(qs) DESC")
    List<LeaderboardEntryDTO> findLeaderboardByTimeRangeAndCategory(
            @Param("startDate") LocalDateTime startDate,
            @Param("category") String category);

    @Query("SELECT new com.DokkaiDorimu.DTO.LeaderboardEntryDTO(" +
            "qs.user.id, " +
            "qs.user.username, " +
            "qs.user.imgUrl, " +
            "'Overall' as category, " +
            "AVG(qs.score), " +
            "COUNT(qs), " +
            "MAX(qs.score), " +
            "SUM(qs.score * 1.0)) " +
            "FROM QuizScore qs " +
            "WHERE qs.completedAt >= :startDate " +
            "GROUP BY qs.user.id, qs.user.username " +
            "ORDER BY AVG(qs.score) DESC, COUNT(qs) DESC")
    List<LeaderboardEntryDTO> findOverallLeaderboardByTimeRange(
            @Param("startDate") LocalDateTime startDate);

    @Query("SELECT DISTINCT qs.category FROM QuizScore qs")
    List<String> findAllCategories();

    @Query("SELECT new com.DokkaiDorimu.DTO.UserTotalScoreDTO(" +
            "qs.user.id, " +
            "qs.user.username, " +
            "qs.user.imgUrl, " +
            "SUM(qs.score * 1.0), " +
            "COUNT(qs), " +
            "AVG(qs.score), " +
            "MAX(qs.score)) " +
            "FROM QuizScore qs " +
            "WHERE qs.completedAt >= :startDate " +
            "GROUP BY qs.user.id, qs.user.username " +
            "ORDER BY SUM(qs.score) DESC")
    List<UserTotalScoreDTO> findTotalScoresForAllUsers(@Param("startDate") LocalDateTime startDate);
}