package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.DTO.LeaderboardEntryDTO;
import com.DokkaiDorimu.DTO.UserTotalScoreDTO;
import com.DokkaiDorimu.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(leaderboardService.getAllCategories());
    }

    @GetMapping("/weekly/{category}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getWeeklyLeaderboard(
            @PathVariable String category) {
        return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard(category));
    }

    @GetMapping("/monthly/{category}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getMonthlyLeaderboard(
            @PathVariable String category) {
        return ResponseEntity.ok(leaderboardService.getMonthlyLeaderboard(category));
    }

    @GetMapping("/all-time/{category}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getAllTimeLeaderboard(
            @PathVariable String category) {
        return ResponseEntity.ok(leaderboardService.getAllTimeLeaderboard(category));
    }

    @GetMapping("/total-scores/{timeRange}")
    public ResponseEntity<List<UserTotalScoreDTO>> getTotalScores(
            @PathVariable String timeRange) {
        try {
            List<UserTotalScoreDTO> totalScores = leaderboardService.getTotalScores(timeRange);
            return ResponseEntity.ok(totalScores);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
