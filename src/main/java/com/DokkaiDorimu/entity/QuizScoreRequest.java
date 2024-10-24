package com.DokkaiDorimu.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizScoreRequest {
    private String category;
    private int score;
    private int totalQuestions;
    private Long articleId;
}
