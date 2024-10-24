package com.DokkaiDorimu.service;

import com.DokkaiDorimu.exception.*;
import com.DokkaiDorimu.repository.ArticleRepository;
import com.DokkaiDorimu.repository.CommentRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.DTO.CommentDTO;
import com.DokkaiDorimu.DTO.NotificationDTO;
import com.DokkaiDorimu.DTO.WebSocketMessage;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.Comment;
import com.DokkaiDorimu.entity.Notification;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    @PersistenceContext
    private EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Transactional
    @Override
    public CommentDTO addComment(Long articleId, Long parentCommentId, String content) {
        User currentUser = getCurrentUser();
        Article article = getArticleById(articleId);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(currentUser);
        comment.setArticle(article);

        if (parentCommentId != null) {
            Comment parentComment = getParentCommentById(parentCommentId);
            comment.setParentComment(parentComment);
            notifyParentCommentAuthor(parentComment, currentUser, article, comment);
        }

        Comment savedComment = commentRepository.save(comment);

        notifyArticleOwner(article, currentUser, savedComment);

        log.info("Comment added: {}", savedComment.getId());
        return convertToDTO(savedComment);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + articleId + " not found"));
    }

    private Comment getParentCommentById(Long parentCommentId) {
        return commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CommentNotFoundException("Parent comment with ID " + parentCommentId + " not found"));
    }

    private void notifyParentCommentAuthor(Comment parentComment, User currentUser, Article article, Comment comment) {
        if (!Objects.equals(parentComment.getUser().getId(), article.getUser().getId()) &&
                !Objects.equals(parentComment.getUser().getId(), currentUser.getId())) {
            Notification notification = notificationService.createNotification(
                    parentComment.getUser(),
                    currentUser,
                    article,
                    comment,
                    currentUser.getUsername() + " replied to your comment on '" + article.getTitle() + "'"
            );
            sendWebSocketNotification(parentComment.getUser(), notification);
            log.info("Notification created for parent comment author: {}", parentComment.getUser().getId());
        }
    }
    private void notifyArticleOwner(Article article, User currentUser, Comment comment) {
        if (!Objects.equals(article.getUser().getId(), currentUser.getId())) {
            Notification notification = notificationService.createNotification(
                    article.getUser(),
                    currentUser,
                    article,
                    comment,
                    currentUser.getUsername() + " commented on your article '" + article.getTitle() + "'"
            );
            sendWebSocketNotification(article.getUser(), notification);
            log.info("Notification created for article owner: {}", article.getUser().getId());
        }
    }
    private void sendWebSocketNotification(User recipient, Notification notification) {
        NotificationDTO notificationDTO = convertToNotificationDTO(notification);
        WebSocketMessage webSocketMessage = new WebSocketMessage("NEW_NOTIFICATION", notificationDTO);
        String destination = "/user/" + recipient.getUsername() + "/topic/notifications";
        messagingTemplate.convertAndSend(destination, webSocketMessage);
        log.info("WebSocket notification sent to: {}", destination);

        // Also update the unread count
        long unreadCount = notificationService.getUnreadCount(recipient);
        WebSocketMessage countMessage = new WebSocketMessage("UNREAD_COUNT", unreadCount);
        String countDestination = "/user/" + recipient.getUsername() + "/topic/notifications/count";
        messagingTemplate.convertAndSend(countDestination, countMessage);
        log.info("WebSocket unread count update sent to: {}", countDestination);
    }

    @Override
    public List<CommentDTO> getCommentsByArticleId(Long articleId) {
        List<Comment> allComments = commentRepository.findByArticleId(articleId);

        // Filter out top-level comments
        List<Comment> topLevelComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .toList();

        // Map each top-level comment to its DTO, ensuring replies are nested
        return topLevelComments.stream()
                .map(comment -> convertToDTO(comment, allComments))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteComment(Long articleId, Long commentId) {
        logger.info("Attempting to delete comment with id {} for article {}", commentId, articleId);

        Comment comment = commentRepository.findByIdAndArticleId(commentId, articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId + " and article id " + articleId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        logger.info("Current user email: {}", currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        logger.info("Current user role: {}", currentUser.getRole());

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isCommentOwner = comment.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isCommentOwner) {
            logger.warn("User {} does not have permission to delete comment {}", currentUserEmail, commentId);
            throw new CommentDeletionException("You don't have permission to delete this comment");
        }

        logger.info("Deleting notifications for comment {} and its replies", commentId);

        // Delete notifications related to the comment and its replies
        Query deleteNotificationsQuery = entityManager.createNativeQuery(
                "DELETE FROM notifications WHERE comment_id = :id OR comment_id IN (SELECT id FROM comment WHERE parent_id = :id)");
        deleteNotificationsQuery.setParameter("id", commentId);
        int deletedNotificationsCount = deleteNotificationsQuery.executeUpdate();

        logger.info("Deleted {} notifications for comment {} and its replies", deletedNotificationsCount, commentId);

        logger.info("Deleting comment {} and its replies", commentId);

        // Use native SQL query to delete replies first
        Query deleteRepliesQuery = entityManager.createNativeQuery("DELETE FROM comment WHERE parent_id = :id");
        deleteRepliesQuery.setParameter("id", commentId);
        int deletedRepliesCount = deleteRepliesQuery.executeUpdate();

        logger.info("Deleted {} replies for comment {}", deletedRepliesCount, commentId);

        // Now delete the parent comment
        Query deleteParentQuery = entityManager.createNativeQuery("DELETE FROM comment WHERE id = :id");
        deleteParentQuery.setParameter("id", commentId);
        int deletedParentCount = deleteParentQuery.executeUpdate();

        // Force a flush and clear of the persistence context
        entityManager.flush();
        entityManager.clear();

        logger.info("Deleted parent comment with id {}", commentId);

        // Verify deletion using a direct database query
        Query checkQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM comment WHERE id = :id OR parent_id = :id");
        checkQuery.setParameter("id", commentId);
        long count = ((Number) checkQuery.getSingleResult()).longValue();

        if (count > 0) {
            logger.error("Failed to delete comment {} and its replies. Some comments still exist in the database.", commentId);
            throw new CommentDeletionException("Failed to delete comment and its replies");
        } else {
            logger.info("Comment {} and its replies deleted successfully", commentId);
        }
    }

    @Override
    public boolean isCommentOwner(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        return comment.getUser().getEmail().equals(userEmail);
    }
    @Override
    public Page<CommentDTO> getCommentsByArticle(Long articleId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByArticleIdAndParentCommentIsNull(articleId, pageable);
        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(comment -> {
                    List<Comment> replies = commentRepository.findByParentCommentId(comment.getId(), Pageable.unpaged()).getContent();
                    List<CommentDTO> replyDTOs = replies.stream()
                            .limit(5)
                            .map(reply -> CommentDTO.fromComment(reply, null))
                            .collect(Collectors.toList());
                    return CommentDTO.fromComment(comment, replyDTOs);
                })
                .collect(Collectors.toList());
        return new PageImpl<>(commentDTOs, pageable, commentPage.getTotalElements());
    }

    @Override
    public Page<CommentDTO> getRepliesByParentComment(Long parentId, Pageable pageable) {
        Page<Comment> replyPage = commentRepository.findByParentCommentId(parentId, pageable);
        List<CommentDTO> replyDTOs = replyPage.getContent().stream()
                .map(reply -> CommentDTO.fromComment(reply, null))
                .collect(Collectors.toList());
        return new PageImpl<>(replyDTOs, pageable, replyPage.getTotalElements());
    }


    private CommentDTO convertToDTO(Comment comment) {
        Long parentCommentId = (comment.getParentComment() != null) ? comment.getParentComment().getId() : null;
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getUser().getId(),
                parentCommentId,
                comment.getUser().getImgUrl(),
                comment.getUser().getRole().toString(),
                comment.getReplies().stream().map(this::convertToDTO).collect(Collectors.toList())
        );
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

    private NotificationDTO convertToNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        // Set recipient details
        if (notification.getRecipient() != null) {
            dto.setRecipientId(notification.getRecipient().getId());
            dto.setRecipientUsername(notification.getRecipient().getUsername());
        }

        // Set sender details
        if (notification.getSender() != null) {
            dto.setSenderId(notification.getSender().getId());
            dto.setSenderUsername(notification.getSender().getUsername());
            dto.setSenderImgUrl(notification.getSender().getImgUrl());
        }

//        // Set article details if present
//        if (notification.getArticle() != null) {
//            dto.setArticleId(notification.getArticle().getId());
//            dto.setArticleTitle(notification.getArticle().getTitle());
//        }

        // Set comment details if present
        if (notification.getComment() != null) {
            dto.setCommentId(notification.getComment().getId());
        }

        return dto;
    }
}