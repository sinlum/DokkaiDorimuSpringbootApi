package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.*;
import com.DokkaiDorimu.entity.*;
import com.DokkaiDorimu.exception.ArticleNotFoundException;
import com.DokkaiDorimu.exception.UnauthorizedException;
import com.DokkaiDorimu.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;
    private final QuizQuestionService quizQuestionService;
    private final QuizQuestionRepository quizQuestionRepository;
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
    private static final long VIEW_TIMEOUT_HOURS = 1;

    @Override
    public Page<ArticleDTO> getAllArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArticleProjection> projectionPage = articleRepository.findAllArticlesWithCounts(pageable);

        return projectionPage.map(projection -> {
            ArticleDTO dto = new ArticleDTO();
            dto.setId(projection.getId());
            dto.setTitle(projection.getTitle());
            dto.setContent(projection.getContent());
            dto.setCategory(projection.getCategory());
            dto.setSubcategory(projection.getSubcategory());
            dto.setImageUrl(projection.getImageUrl());
            dto.setUsername(projection.getUsername());
            dto.setAuthorImageUrl(projection.getUserImgUrl());
            dto.setCommentCount(projection.getCommentCount());
            dto.setLikeCount(projection.getLikeCount());
            dto.setCreatedAt(projection.getCreatedAt());
            dto.setViewCount(projection.getViewCount());
            dto.setBookmarkCount(projection.getBookmarkCount());
            return dto;
        });
    }

    @Override
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findByIdWithComments(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));
        return convertToDTO(article);
    }

    @Override
    public ArticleDTO getArticleById(Long id, String viewerIdentifier) {
        Article article = articleRepository.findByIdWithComments(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));

        if (!isDuplicateView(article, viewerIdentifier)) {
            createNewView(article, viewerIdentifier);
        }

        return convertToDTO(article);
    }

    private boolean isDuplicateView(Article article, String viewerIdentifier) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(VIEW_TIMEOUT_HOURS);
        return viewRepository.existsByArticleAndViewerIdentifierAndViewedAtAfter(article, viewerIdentifier, cutoffTime);
    }

    private void createNewView(Article article, String viewerIdentifier) {
        View view = new View();
        view.setArticle(article);
        view.setViewerIdentifier(viewerIdentifier);
        view.setViewedAt(LocalDateTime.now());
        viewRepository.save(view);
    }


    public int getArticleViewCount(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));
        return article.getViewCount();
    }


    @Override
    public ArticleDTO saveArticle(ArticleDTO articleDTO, MultipartFile image, MultipartFile audio, List<QuizQuestionDTO> quizQuestions) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Article article = new Article();
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setCategory(articleDTO.getCategory());
        article.setSubcategory(articleDTO.getSubcategory());

        if (image != null && !image.isEmpty()) {
            try {
                String imgUrl = saveImage(image);
                article.setImageUrl(imgUrl);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        if (audio != null && !audio.isEmpty()) {
            try {
                String audioUrl = saveAudio(audio);
                article.setAudioUrl(audioUrl);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        article.setUser(currentUser);
        article.setCreatedAt(LocalDateTime.now());

        Article savedArticle = articleRepository.save(article);

        // Save quiz questions
        saveQuizQuestions(savedArticle, quizQuestions);

        ArticleDTO savedArticleDTO = convertToDTO(savedArticle);
        savedArticleDTO.setAuthorImageUrl(currentUser.getImgUrl());

        return savedArticleDTO;
    }

    private void saveQuizQuestions(Article article, List<QuizQuestionDTO> quizQuestions) {
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
    private String saveImage(MultipartFile file) throws IOException {
        String folder = "src/main/resources/static/images/";
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = folder + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());

        return "/images/" + file.getOriginalFilename(); // Return the path to the stored image
    }

    private String saveAudio(MultipartFile file) throws IOException {
        String folder = "src/main/resources/static/audio/";
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = folder + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());

        return "/audio/" + file.getOriginalFilename();
    }

    @Transactional
    @Override
    public ArticleDTO updateArticle(Long id, ArticleDTO updatedArticleDTO, MultipartFile image, MultipartFile audio, List<QuizQuestionDTO> quizQuestions) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingArticle.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("You are not allowed to edit this article");
        }

        existingArticle.setTitle(updatedArticleDTO.getTitle());
        existingArticle.setContent(updatedArticleDTO.getContent());
        existingArticle.setCategory(updatedArticleDTO.getCategory());
        existingArticle.setSubcategory(updatedArticleDTO.getSubcategory());

        // Handle image update
        if (image != null && !image.isEmpty()) {
            try {
                // Delete old image if it exists
                if (existingArticle.getImageUrl() != null) {
                    deleteImage(existingArticle.getImageUrl());
                }

                String imgUrl = saveImage(image);
                existingArticle.setImageUrl(imgUrl);
            } catch (Exception e) {
                System.out.println("Error updating image: " + e.getMessage());
            }
        }

        // Handle audio update
        if (audio != null && !audio.isEmpty()) {
            try {
                // Delete old audio if it exists
                if (existingArticle.getAudioUrl() != null) {
                    deleteAudio(existingArticle.getAudioUrl());
                }

                String audioUrl = saveAudio(audio);
                existingArticle.setAudioUrl(audioUrl);
            } catch (Exception e) {
                System.out.println("Error updating audio: " + e.getMessage());
            }
        }

        Article savedArticle = articleRepository.save(existingArticle);

        // Update quiz questions
        updateQuizQuestions(savedArticle, quizQuestions);

        ArticleDTO savedArticleDTO = convertToDTO(savedArticle);
        savedArticleDTO.setAuthorImageUrl(currentUser.getImgUrl());

        return savedArticleDTO;
    }


    private void updateQuizQuestions(Article article, List<QuizQuestionDTO> quizQuestions) {
        // Delete existing quiz questions
        quizQuestionRepository.deleteByArticleId(article.getId());

        // Save new quiz questions
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


    private void deleteAudio(String audioUrl) {
        if (audioUrl != null && !audioUrl.isEmpty()) {
            try {
                String filePath = "src/main/resources/static" + audioUrl;
                Path path = Paths.get(filePath);

                if (Files.exists(path)) {
                    Files.delete(path);
                    System.out.println("Deleted old audio: " + filePath);
                } else {
                    System.out.println("Old audio file not found: " + filePath);
                }
            } catch (IOException e) {
                System.err.println("Error deleting old audio: " + e.getMessage());
            }
        }
    }
    private void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String folder = "src/main/resources/static";
            File file = new File(folder + imageUrl);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingArticle.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("You are not authorized to delete this article");
        }

        // Delete all bookmarks associated with this article
        bookmarkRepository.deleteByArticleId(id);
        commentRepository.deleteByArticleId(id);
        likeRepository.deleteByArticleId(id);
        // Delete the associated image file if it exists
        if (existingArticle.getImageUrl() != null && !existingArticle.getImageUrl().isEmpty()) {
            deleteImage(existingArticle.getImageUrl());
        }

        articleRepository.deleteById(id);
    }


    @Override
    public List<ArticleDTO> getArticlesByCategory(String category) {
        return articleRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleDTO> getArticlesBySubcategory(String subcategory) {
        return articleRepository.findBySubcategory(subcategory).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleDTO> searchByTitleAndContent(String keyword) {
        try {
            return articleRepository.searchByTitleAndContent(keyword).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            logger.error("Error searching articles by keyword '{}': {}", keyword, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public int getLikeCount(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));
        return article.getLikeCount();
    }

    @Override
    public List<CommentDTO> getComments(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));
        return article.getComments().stream()
                .filter(comment -> comment.getParentComment() == null) // Only top-level comments
                .map(comment -> convertToDTO(comment, article.getComments()))
                .collect(Collectors.toList());
    }

    @Override
    public DashboardArticlesDTO getDashboardArticles() {
        // Get latest 3 articles
        List<ArticleDTO> latestArticles = articleRepository
                .findLatestArticles(PageRequest.of(0, 3))
                .stream()
                .map(projection -> {
                    ArticleDTO dto = new ArticleDTO();
                    dto.setId(projection.getId());
                    dto.setTitle(projection.getTitle());
                    dto.setContent(projection.getContent());
                    dto.setCategory(projection.getCategory());
                    dto.setSubcategory(projection.getSubcategory());
                    dto.setImageUrl(projection.getImageUrl());
                    dto.setUsername(projection.getUsername());
                    dto.setAuthorImageUrl(projection.getUserImgUrl());
                    dto.setCommentCount(projection.getCommentCount());
                    dto.setLikeCount(projection.getLikeCount());
                    dto.setCreatedAt(projection.getCreatedAt());
                    dto.setViewCount(projection.getViewCount());
                    dto.setBookmarkCount(projection.getBookmarkCount());
                    return dto;
                })
                .collect(Collectors.toList());

        // Get top 3 hot articles
        List<ArticleDTO> hotArticles = articleRepository
                .findHotArticles(PageRequest.of(0, 3))
                .stream()
                .map(projection -> {
                    ArticleDTO dto = new ArticleDTO();
                    dto.setId(projection.getId());
                    dto.setTitle(projection.getTitle());
                    dto.setContent(projection.getContent());
                    dto.setCategory(projection.getCategory());
                    dto.setSubcategory(projection.getSubcategory());
                    dto.setImageUrl(projection.getImageUrl());
                    dto.setUsername(projection.getUsername());
                    dto.setAuthorImageUrl(projection.getUserImgUrl());
                    dto.setCommentCount(projection.getCommentCount());
                    dto.setLikeCount(projection.getLikeCount());
                    dto.setCreatedAt(projection.getCreatedAt());
                    dto.setViewCount(projection.getViewCount());
                    dto.setBookmarkCount(projection.getBookmarkCount());
                    return dto;
                })
                .collect(Collectors.toList());

        return new DashboardArticlesDTO(latestArticles, hotArticles);
    }


    private ArticleDTO convertToDTO(Article article) {
        List<QuizQuestionDTO> quizQuestionDTOs = article.getQuizQuestions().stream()
                .map(this::convertToQuizQuestionDTO)
                .collect(Collectors.toList());
        return new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getCategory(),
                article.getSubcategory(),
                article.getImageUrl(),
                article.getUser().getUsername(),
                article.getUser().getId(),
                article.getUser().getStatus(),
                article.getUser().getImgUrl(),
                article.getAudioUrl(),
                quizQuestionDTOs,
//                article.getComments().stream()
//                        .filter(comment -> comment.getParentComment() == null) // Only top-level comments
//                        .map(comment -> convertToDTO(comment, article.getComments()))
//                        .collect(Collectors.toList()),
                article.getComments().size(),
                article.getLikeCount(),
                article.getCreatedAt(),
                article.getViewCount(),
                article.getBookmarkCount()
        );
    }

    private QuizQuestionDTO convertToQuizQuestionDTO(QuizQuestion quizQuestion) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(quizQuestion.getId());
        dto.setText(quizQuestion.getText());
        dto.setType(quizQuestion.getType());
        dto.setCorrectAnswer(quizQuestion.getCorrectAnswer());
        if (quizQuestion.getType().equals("multiple")) {
            dto.setAnswers(Arrays.asList(quizQuestion.getAnswers().split("\\|")));
        }
        return dto;
    }

    private CommentDTO convertToDTO(Comment comment, List<Comment> allComments) {
        Long parentCommentId = (comment.getParentComment() != null) ? comment.getParentComment().getId() : null;

        List<CommentDTO> replies = allComments.stream()
                .filter(reply -> reply.getParentComment() != null && reply.getParentComment().getId().equals(comment.getId()))
                .map(reply -> convertToDTO(reply, allComments))
                .collect(Collectors.toList());

        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getUser().getId(),
                parentCommentId,
                comment.getUser().getImgUrl(),
                comment.getUser().getRole().toString(),
                replies
        );
    }
}