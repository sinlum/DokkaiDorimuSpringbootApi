package com.DokkaiDorimu.service;
import com.DokkaiDorimu.DTO.*;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.QuizScore;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.ResourceNotFoundException;
import com.DokkaiDorimu.repository.ArticleRepository;
import com.DokkaiDorimu.repository.QuizScoreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class QuizScoreServiceImpl implements QuizScoreService {
    private final QuizScoreRepository quizScoreRepository;
    private final ArticleRepository articleRepository;
    private static final Logger logger = LoggerFactory.getLogger(QuizScoreServiceImpl.class);


    public QuizScore saveQuizScore(User user, String category, int score, int totalQuestions, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));
        QuizScore quizScore = new QuizScore();
        quizScore.setUser(user);
        quizScore.setCategory(category);
        quizScore.setScore(score);
        quizScore.setTotalQuestions(totalQuestions);
        quizScore.setCompletedAt(LocalDateTime.now());
        quizScore.setArticle(article);
        quizScore.setArticleRead(true);
        return quizScoreRepository.save(quizScore);
    }

    @Override
    public List<QuizScore> getQuizScoresByUser(User user) {
        logger.info("Fetching quiz scores for user ID: {}", user.getId());
        List<QuizScore> scores = quizScoreRepository.findByUserIdWithArticles(user.getId());
        logger.debug("Found {} quiz scores", scores.size());
        return scores;
    }

    public List<QuizScore> getQuizScoresByUserAndCategory(User user, String category) {
        return quizScoreRepository.findByUserAndCategory(user, category);
    }

    public List<CategoryScoreDTO> getAverageScoresByCategory(User user) {
        return quizScoreRepository.getAverageScoresByCategory(user);
    }

    public Double getOverallAverageScore(User user) {
        return quizScoreRepository.getOverallAverageScore(user);
    }

    @Override
    public ReadingStatisticsDTO getReadingStatistics(User user) {
        try {
            // Get total unique articles read
            long totalArticlesRead = quizScoreRepository.countUniqueArticlesReadByUser(user.getId());
            logger.debug("Found {} unique articles read by user {}", totalArticlesRead, user.getId());

            // Get category breakdown
            Map<String, Long> categoryBreakdown = new HashMap<>();
            try {
                List<Object[]> categoryStats = quizScoreRepository.getArticleCountByCategory(user.getId());
                if (categoryStats != null) {
                    categoryBreakdown = categoryStats.stream()
                            .filter(stat -> stat[0] != null)
                            .collect(Collectors.toMap(
                                    stat -> (String) stat[0],
                                    stat -> (Long) stat[1],
                                    (v1, v2) -> v1,
                                    HashMap::new
                            ));
                }
            } catch (Exception e) {
                logger.warn("Error getting category breakdown for user {}", user.getId(), e);
            }

            // Get recent articles
            List<RecentArticleDTO> recentlyRead = new ArrayList<>();
            try {
                List<Object[]> recentArticles = quizScoreRepository.findRecentlyReadArticles(user.getId());
                if (recentArticles != null) {
                    recentlyRead = recentArticles.stream()
                            .filter(article -> article != null && article.length >= 4)
                            .limit(5)
                            .map(article -> new RecentArticleDTO(
                                    toLong(article[0]),
                                    toString(article[1]),
                                    toString(article[2]),
                                    toDateTime(article[3])
                            ))
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                logger.warn("Error getting recently read articles for user {}", user.getId(), e);
            }

            // Calculate streak
            long currentStreak = calculateReadingStreak(quizScoreRepository.findByUserIdWithArticles(user.getId()));

            return new ReadingStatisticsDTO(
                    totalArticlesRead,
                    categoryBreakdown,
                    currentStreak,
                    recentlyRead
            );
        } catch (Exception e) {
            logger.error("Error calculating reading statistics for user {}", user.getId(), e);
            throw new RuntimeException("Failed to calculate reading statistics", e);
        }
    }

    private long calculateReadingStreak(List<QuizScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }

        List<LocalDate> readingDates = scores.stream()
                .map(score -> Optional.ofNullable(score.getCompletedAt())
                        .orElse(LocalDateTime.now())
                        .toLocalDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (readingDates.isEmpty()) {
            return 0;
        }

        LocalDate currentDate = LocalDate.now();
        if (!readingDates.get(0).equals(currentDate)) {
            return 0;
        }

        long streak = 1;
        LocalDate expectedDate = currentDate;

        for (LocalDate date : readingDates) {
            if (date.equals(expectedDate)) {
                expectedDate = expectedDate.minusDays(1);
                streak++;
            } else {
                break;
            }
        }

        return Math.max(0, streak - 1);
    }

    // Helper methods for safe type conversion
    private Long toLong(Object obj) {
        return obj != null ? ((Number) obj).longValue() : 0L;
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private LocalDateTime toDateTime(Object obj) {
        return obj != null ? (LocalDateTime) obj : LocalDateTime.now();
    }

    public ScoreSummaryDTO getScoreSummary(User user) {
        try {
            logger.info("Fetching score summary for user: {}", user.getEmail());

            // Convert List<CategoryScoreDTO> to Map<String, Double>
            List<CategoryScoreDTO> categoryScoreDTOs = quizScoreRepository.getAverageScoresByCategory(user);
            Map<String, Double> categoryScores = categoryScoreDTOs.stream()
                    .collect(Collectors.toMap(
                            CategoryScoreDTO::getCategory,
                            CategoryScoreDTO::getAverageScore
                    ));

            // Get overall average score
            Double overallAverage = quizScoreRepository.getOverallAverageScore(user);
            if (overallAverage == null) {
                overallAverage = 0.0;
            }

            // Get total quizzes taken
            long totalQuizzes = quizScoreRepository.countByUser(user);

            return new ScoreSummaryDTO(
                    categoryScores,
                    overallAverage,
                    totalQuizzes
            );
        } catch (Exception e) {
            logger.error("Error fetching score summary for user: {}", user.getEmail(), e);
            throw new RuntimeException("スコアサマリーの取得中にエラーが発生しました。", e);
        }
    }
}

