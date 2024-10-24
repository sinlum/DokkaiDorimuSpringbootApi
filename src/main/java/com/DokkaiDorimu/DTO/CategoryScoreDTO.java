package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryScoreDTO {
    private String category;
    private Double averageScore;
}