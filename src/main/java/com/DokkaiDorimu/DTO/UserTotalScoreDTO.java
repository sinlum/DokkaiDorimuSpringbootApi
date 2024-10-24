package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTotalScoreDTO {
    private Long userId;
    private String username;
    private String imgUrl;
    private Double totalScore;
    private Long totalQuizzes;
    private Double averageScore;
    private Integer highestScore;
}

