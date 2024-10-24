package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.UserRoleUpdateDTO;
import com.DokkaiDorimu.repository.*;
import com.DokkaiDorimu.DTO.ArticleDTO;
import com.DokkaiDorimu.DTO.ContributionDTO;
import com.DokkaiDorimu.DTO.UserDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.UserNotFoundException;
import com.DokkaiDorimu.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArticleRepository articleRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final BookmarkRepository bookmarkRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public Optional<User> getUserByEmail(String email) {
        System.out.println("Searching for user with email: " + email);
        Optional<User> user = userRepository.findByEmail(email);
        System.out.println("User found: " + user.isPresent());
        return user;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        System.out.println("Searching for user with username: " + username);
        Optional<User> user = userRepository.findByUsername(username);
        System.out.println("User found: " + user.isPresent());
        return user;
    }

    @Override
    public User saveUser(User user) {
        if(user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found");
        }
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserRoleUpdateDTO updateUserRole(Long userId, User.Role newRole) {
        try {
            logger.info("Updating role for user {} to {}", userId, newRole);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            user.setRole(newRole);
            User savedUser = userRepository.save(user);

            // Convert to DTO to avoid circular references
            UserRoleUpdateDTO dto = new UserRoleUpdateDTO();
            dto.setId(savedUser.getId());
            dto.setUsername(savedUser.getUsername());
            dto.setRole(savedUser.getRole().name());
            dto.setEmail(savedUser.getEmail());
            dto.setImgUrl(savedUser.getImgUrl());
            dto.setStatus(savedUser.getStatus());

            logger.info("Successfully updated role for user {} to {}", userId, newRole);
            return dto;

        } catch (Exception e) {
            logger.error("Error updating role for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user role", e);
        }
    }

    @Override
    public void signup(UserDTO userDTO) throws Exception {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new Exception("Email already in use");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setImgUrl(null); // Set imgUrl to null
        user.setRole(User.Role.USER); // Set default role to USER

        userRepository.save(user);
    }
    @Override
    public void updateUserProfile(UserDTO userDTO, String email, String status, String address, String bio, String school, String grade) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setUsername(userDTO.getUsername());
        user.setStatus(userDTO.getStatus());
        user.setBio(userDTO.getBio());
        user.setAddress(userDTO.getAddress());
        user.setSchool(userDTO.getSchool());
        user.setGrade(userDTO.getGrade());

        if (userDTO.getImgUrl() != null) {
            user.setImgUrl(userDTO.getImgUrl());
        }

        userRepository.save(user);
    }

    @Override
    public UserDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setImgUrl(user.getImgUrl());
        userDTO.setStatus(user.getStatus());
        userDTO.setAddress(user.getAddress());
        userDTO.setBio(user.getBio());
        userDTO.setGrade(user.getGrade());
        userDTO.setSchool(user.getSchool());
        userDTO.setRole(user.getRole().toString());
        List<Article> userArticles = articleRepository.findByUser(user);
        List<ArticleDTO> articleDTOs = userArticles.stream()
                .map(this::convertToArticleDTO)
                .collect(Collectors.toList());
        userDTO.setArticles(articleDTOs);

        return userDTO;
    }

    @Override
    public List<UserDTO> getAllCreators() {
        List<User> creators = userRepository.findByRole(User.Role.CREATOR);
        return creators.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserProfile(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setImgUrl(user.getImgUrl());
        userDTO.setStatus(user.getStatus());
        userDTO.setAddress(user.getAddress());
        userDTO.setBio(user.getBio());
        userDTO.setGrade(user.getGrade());
        userDTO.setSchool(user.getSchool());
        userDTO.setRole(user.getRole().toString());

        List<Article> userArticles = articleRepository.findByUser(user);
        List<ArticleDTO> articleDTOs = userArticles.stream()
                .map(this::convertToArticleDTO)
                .collect(Collectors.toList());
        userDTO.setArticles(articleDTOs);
        userDTO.setOnline(user.isOnline());

        return userDTO;
    }

    public List<ContributionDTO> getContributionsByCreator(Long creatorId) {
        List<Article> articles = articleRepository.findByUserId(creatorId);

        return articles.stream().map(article -> {
            int likeCount = likeRepository.countByArticleId(article.getId());
            int commentCount = commentRepository.countByArticleId(article.getId());
            int bookmarkCount = bookmarkRepository.countByArticleId(article.getId());

            return new ContributionDTO(
                    article.getId(),
                    article.getTitle(),
                    likeCount,
                    commentCount,
                    bookmarkCount,
                    article.getViewCount(),
                    article.getCategory(),
                    article.getImageUrl(),
                    article.getContent(),
                    article.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }
    public List<UserDTO> searchByEmail(String email) {
        List<User> users = userRepository.findByEmailContainingIgnoreCase(email);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    @Override
    public List<UserDTO> getAllUsersExcept(Long userId) {
        List<User> users = userRepository.findByIdNot(userId);
        for (User user : users) {
            logger.info("User {} online status: {}", user.getId(), user.isOnline());
        }
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
//    @Override
//    @Transactional
//    public void setUserOnlineStatus(Long userId, boolean isOnline) {
//        try {
//            logger.debug("Attempting to set online status for user {} to {}", userId, isOnline);
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
//            logger.debug("User found: {}", user);
//            user.setOnline(isOnline);
//            User savedUser = userRepository.save(user);
//            logger.debug("User saved: {}", savedUser);
//            logger.info("User {} online status set to {}", userId, isOnline);
//        } catch (Exception e) {
//            logger.error("Error setting online status for user {}", userId, e);
//            throw new RuntimeException("Failed to update user online status", e);
//        }
//    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImgUrl(user.getImgUrl());
        dto.setStatus(user.getStatus());
        dto.setAddress(user.getAddress());
        dto.setBio(user.getBio());
        dto.setSchool(user.getSchool());
        dto.setGrade(user.getGrade());
        dto.setRole(user.getRole().name());
        dto.setOnline(user.isOnline());
        // Get article count and set it in DTO
        List<Article> userArticles = articleRepository.findByUser(user);
        dto.setArticleCount(userArticles.size());

        // Optional: Calculate and set additional statistics
        int totalViews = 0;
        int totalLikes = 0;
        int totalComments = 0;

        for (Article article : userArticles) {
            totalViews += article.getViewCount();
            totalLikes += article.getLikes().size();
            totalComments += article.getComments().size();
        }

        dto.setTotalViews(totalViews);
        dto.setTotalLikes(totalLikes);
        dto.setTotalComments(totalComments);

        return dto;
    }
    // For performance optimization, you might want to add this method
    private UserDTO convertToDTOWithoutArticles(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImgUrl(user.getImgUrl());
        dto.setStatus(user.getStatus());
        dto.setAddress(user.getAddress());
        dto.setBio(user.getBio());
        dto.setSchool(user.getSchool());
        dto.setGrade(user.getGrade());
        dto.setRole(user.getRole().name());
        dto.setOnline(user.isOnline());

        // Use count query instead of fetching all articles
        long articleCount = articleRepository.countByUser(user);
        dto.setArticleCount((int) articleCount);

        return dto;
    }
    private ArticleDTO convertToArticleDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setCategory(article.getCategory());
        dto.setSubcategory(article.getSubcategory());
        dto.setImageUrl(article.getImageUrl());
        dto.setUsername(article.getUser().getUsername());
        dto.setAuthorImageUrl(article.getUser().getImgUrl());
        dto.setCommentCount(article.getComments().size());
        dto.setLikeCount(article.getLikes().size());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setViewCount(article.getViewCount());
        dto.setBookmarkCount(article.getBookmarks().size());
        // Note: We're not setting comments here to avoid potential circular references
        return dto;
    }
}
