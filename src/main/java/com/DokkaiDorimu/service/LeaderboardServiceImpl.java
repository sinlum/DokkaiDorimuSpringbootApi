package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.LeaderboardEntryDTO;
import com.DokkaiDorimu.DTO.UserTotalScoreDTO;
import com.DokkaiDorimu.repository.QuizScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private final QuizScoreRepository quizScoreRepository;

    // Helper method to get a reasonable "all-time" start date
    private LocalDateTime getAllTimeStartDate() {
        // Using year 2000 as a reasonable start date
        return LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    }

    public List<LeaderboardEntryDTO> getWeeklyLeaderboard(String category) {
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);

        return category.equals("Overall")
                ? quizScoreRepository.findOverallLeaderboardByTimeRange(startOfWeek)
                : quizScoreRepository.findLeaderboardByTimeRangeAndCategory(startOfWeek, category);
    }

    public List<LeaderboardEntryDTO> getMonthlyLeaderboard(String category) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0);

        return category.equals("Overall")
                ? quizScoreRepository.findOverallLeaderboardByTimeRange(startOfMonth)
                : quizScoreRepository.findLeaderboardByTimeRangeAndCategory(startOfMonth, category);
    }

    public List<LeaderboardEntryDTO> getAllTimeLeaderboard(String category) {
        LocalDateTime startDate = getAllTimeStartDate();
        return category.equals("Overall")
                ? quizScoreRepository.findOverallLeaderboardByTimeRange(startDate)
                : quizScoreRepository.findLeaderboardByTimeRangeAndCategory(startDate, category);
    }

    public List<String> getAllCategories() {
        return quizScoreRepository.findAllCategories();
    }

    public List<UserTotalScoreDTO> getTotalScores(String timeRange) {
        LocalDateTime startDate;

        switch (timeRange.toLowerCase()) {
            case "weekly":
                startDate = LocalDateTime.now()
                        .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                        .withHour(0).withMinute(0).withSecond(0);
                break;
            case "monthly":
                startDate = LocalDateTime.now()
                        .with(TemporalAdjusters.firstDayOfMonth())
                        .withHour(0).withMinute(0).withSecond(0);
                break;
            case "all-time":
                startDate = getAllTimeStartDate();
                break;
            default:
                throw new IllegalArgumentException("Invalid time range. Use 'weekly', 'monthly', or 'all-time'");
        }

        return quizScoreRepository.findTotalScoresForAllUsers(startDate);
    }
}
