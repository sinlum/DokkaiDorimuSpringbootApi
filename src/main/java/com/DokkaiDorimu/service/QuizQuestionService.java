package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.QuizQuestionDTO;
import com.DokkaiDorimu.entity.Article;

import java.util.List;

public interface QuizQuestionService {
    public void saveQuizQuestions(Article article, List<QuizQuestionDTO> quizQuestions);
}
