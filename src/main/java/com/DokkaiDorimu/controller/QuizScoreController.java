package com.DokkaiDorimu.controller;
import com.DokkaiDorimu.DTO.CategoryScoreDTO;
import com.DokkaiDorimu.DTO.ReadingStatisticsDTO;
import com.DokkaiDorimu.DTO.ScoreSummaryDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.QuizScore;
import com.DokkaiDorimu.entity.QuizScoreRequest;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.service.ArticleServiceImpl;
import com.DokkaiDorimu.service.QuizScoreService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz-scores")
public class QuizScoreController {
    private final QuizScoreService quizScoreService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(QuizScoreController.class);

    @PostMapping
    public ResponseEntity<QuizScore> saveQuizScore(@RequestBody QuizScoreRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            logger.info("Current user email: {}", currentUserEmail);

            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            logger.info("Retrieved user: {}", currentUser);

            QuizScore savedScore = quizScoreService.saveQuizScore(currentUser, request.getCategory(), request.getScore(), request.getTotalQuestions(),request.getArticleId());
            return ResponseEntity.ok(savedScore);
        } catch (Exception e) {
            logger.error("Error saving quiz score", e);
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<QuizScore>> getQuizScores(@AuthenticationPrincipal User user) {
        List<QuizScore> scores = quizScoreService.getQuizScoresByUser(user);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<QuizScore>> getQuizScoresByCategory(
            @PathVariable String category) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<QuizScore> scores = quizScoreService.getQuizScoresByUserAndCategory(currentUser, category);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/average-by-category")
    public ResponseEntity<List<CategoryScoreDTO>> getAverageScoresByCategory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<CategoryScoreDTO> averageScores = quizScoreService.getAverageScoresByCategory(currentUser);
        return ResponseEntity.ok(averageScores);
    }

    @GetMapping("/overall-average")
    public ResponseEntity<Double> getOverallAverageScore() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Double overallAverage = quizScoreService.getOverallAverageScore(currentUser);
        return ResponseEntity.ok(overallAverage);
    }

    @GetMapping("/reading-stats")
    public ResponseEntity<ReadingStatisticsDTO> getReadingStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ReadingStatisticsDTO stats = quizScoreService.getReadingStatistics(currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getUserScoreSummary() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            ScoreSummaryDTO summary = quizScoreService.getScoreSummary(currentUser);

            logger.info("Found scores in {} categories for user",
                    summary.getCategoryScores().size());

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error fetching score summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}




