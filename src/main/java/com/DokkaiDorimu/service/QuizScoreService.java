package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.CategoryScoreDTO;
import com.DokkaiDorimu.DTO.ReadingStatisticsDTO;
import com.DokkaiDorimu.DTO.ScoreSummaryDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.QuizScore;
import com.DokkaiDorimu.entity.User;

import java.util.List;

public interface QuizScoreService {
    QuizScore saveQuizScore(User user, String category, int score, int totalQuestions, Long articleId);
    List<QuizScore> getQuizScoresByUser(User user);
    List<QuizScore> getQuizScoresByUserAndCategory(User user, String category);
    List<CategoryScoreDTO> getAverageScoresByCategory(User user);
    Double getOverallAverageScore(User user);
    ReadingStatisticsDTO getReadingStatistics(User user);
    ScoreSummaryDTO getScoreSummary(User user);

}
