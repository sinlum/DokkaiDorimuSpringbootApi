package com.DokkaiDorimu.DTO;

import lombok.Data;

import java.util.List;
@Data
public class QuizQuestionDTO {
    private Long id;
    private String text;
    private String type;
    private List<String> answers;
    private String correctAnswer;
}
