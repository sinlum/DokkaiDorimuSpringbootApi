package com.DokkaiDorimu.service;

import com.DokkaiDorimu.repository.NotificationRepository;
import com.DokkaiDorimu.DTO.CombinedNotificationUpdate;
import com.DokkaiDorimu.DTO.NotificationDTO;
import com.DokkaiDorimu.DTO.WebSocketMessage;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.Comment;
import com.DokkaiDorimu.entity.Notification;
import com.DokkaiDorimu.entity.User;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Override
    @Transactional
    public Notification createNotification(User recipient, User sender, Article article, Comment comment, String message) {
        logger.info("Starting createNotification for recipient: {}, sender: {}, article: {}, comment: {}",
                recipient.getUsername(), sender.getUsername(), article.getId(), comment != null ? comment.getId() : "null");

        // Check if a similar notification already exists
        List<Notification> existingNotifications = notificationRepository.findByRecipientAndSenderAndArticleAndCommentAndIsReadFalse(
                recipient, sender, article, comment);

        if (!existingNotifications.isEmpty()) {
            logger.info("Similar unread notification already exists. Skipping creation.");
            return existingNotifications.get(0);
        }

        logger.info("Creating notification for user: {}", recipient.getUsername());
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setArticle(article);
        notification.setComment(comment);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Saved notification with ID: {}", savedNotification.getId());

        // Send WebSocket notification after successful save
        NotificationDTO notificationDTO = convertToDTO(savedNotification);
        sendWebSocketNotification(recipient, "NEW_NOTIFICATION", notificationDTO);

        // Update unread count
        long unreadCount = notificationRepository.countByRecipientAndIsReadFalse(recipient);
        logger.info("Current unread count for user {}: {}", recipient.getUsername(), unreadCount);

        // Send a combined update
        CombinedNotificationUpdate combinedUpdate = new CombinedNotificationUpdate(notificationDTO, unreadCount);
        sendWebSocketNotification(recipient, "NOTIFICATION_UPDATE", combinedUpdate);

        logger.info("Notification created and combined WebSocket message sent successfully");
        return savedNotification;
    }

    private void sendWebSocketNotification(User recipient, String type, Object payload) {
        WebSocketMessage webSocketMessage = new WebSocketMessage(type, payload);
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/topic/notifications",
                webSocketMessage
        );
        logger.info("WebSocket message of type {} sent to user {}", type, recipient.getUsername());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(User recipient) {
        List<Notification> notifications = notificationRepository.findByRecipientAndIsReadFalse(recipient);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsForUser(User user) {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(User recipient) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAndIsReadFalse(recipient);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User recipient) {
        logger.info("Fetching unread count for user: {}", recipient.getUsername());
        long count = notificationRepository.countByRecipientAndIsReadFalse(recipient);
        logger.info("Unread count for user {}: {}", recipient.getUsername(), count);
        return count;
    }

//    @Override
//    public long getUnreadCount(User recipient) {
//        long count = notificationRepository.countByRecipientAndIsReadFalse(recipient);
//
//        // Send WebSocket message with updated count
//        WebSocketMessage webSocketMessage = new WebSocketMessage("UNREAD_COUNT", count);
//        messagingTemplate.convertAndSendToUser(
//                recipient.getUsername(),
//                "/topic/notifications/count",
//                webSocketMessage
//        );
//
//        return count;
//    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setRecipientUsername(notification.getRecipient().getUsername());
        dto.setSenderId(notification.getSender().getId());
        dto.setSenderUsername(notification.getSender().getUsername());
        dto.setSenderImgUrl(notification.getSender().getImgUrl());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        if (notification.getArticle() != null) {
            dto.setArticleId(notification.getArticle().getId());
        }
        if (notification.getComment() != null) {
            dto.setCommentId(notification.getComment().getId());
        }
        return dto;
    }
}