package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.LeaderboardEntryDTO;
import com.DokkaiDorimu.DTO.UserTotalScoreDTO;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntryDTO> getWeeklyLeaderboard(String category);
    List<LeaderboardEntryDTO> getMonthlyLeaderboard(String category);
    List<LeaderboardEntryDTO> getAllTimeLeaderboard(String category);
    List<String> getAllCategories();
    List<UserTotalScoreDTO> getTotalScores(String timeRange);
}
