package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntryDTO {
    private Long userId;
    private String username;
    private String imgUrl;
    private String category;
    private Double averageScore;
    private Long totalQuizzes;
    private Integer highestScore;
    private Double totalScore;
}
