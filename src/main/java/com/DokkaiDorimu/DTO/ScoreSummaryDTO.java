package com.DokkaiDorimu.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
public class ScoreSummaryDTO {
    private Map<String, Double> categoryScores;  // Changed to Map to match your requirement
    private Double overallAverageScore;
    private int totalQuizzesTaken;  // Changed to int to match your requirement

    public ScoreSummaryDTO(Map<String, Double> categoryScores,
                           Double overallAverageScore,
                           long totalQuizzesTaken) {  // Accept long but convert to int
        this.categoryScores = categoryScores;
        this.overallAverageScore = overallAverageScore;
        this.totalQuizzesTaken = (int) totalQuizzesTaken;  // Convert long to int
    }
}
