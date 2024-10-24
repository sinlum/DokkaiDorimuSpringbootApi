package com.DokkaiDorimu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "quiz_question")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text is mandatory")
    @Column(columnDefinition = "TEXT")
    private String text;

    @NotBlank(message = "Question type is mandatory")
    private String type; // "truefalse" or "multiple"

    @Column(columnDefinition = "TEXT")
    private String answers; // For multiple choice, store answers as pipe-separated string

    @NotBlank(message = "Correct answer is mandatory")
    private String correctAnswer;

    @ManyToOne
    @JoinColumn(name = "article_id")
    @JsonIgnore
    private Article article;
}
