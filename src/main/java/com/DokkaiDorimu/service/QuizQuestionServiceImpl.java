package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.QuizQuestionDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.QuizQuestion;
import com.DokkaiDorimu.repository.QuizQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class QuizQuestionServiceImpl implements QuizQuestionService {

    private final QuizQuestionRepository quizQuestionRepository;
    @Override
    public void saveQuizQuestions(Article article, List<QuizQuestionDTO> quizQuestions) {
        for (QuizQuestionDTO questionDTO : quizQuestions) {
            QuizQuestion question = new QuizQuestion();
            question.setArticle(article);
            question.setText(questionDTO.getText());
            question.setType(questionDTO.getType());
            question.setCorrectAnswer(questionDTO.getCorrectAnswer());

            if (questionDTO.getType().equals("multiple")) {
                question.setAnswers(String.join("|", questionDTO.getAnswers()));
            }

            quizQuestionRepository.save(question);
        }
    }
}
