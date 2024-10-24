package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.NotificationDTO;
import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.Comment;
import com.DokkaiDorimu.entity.Notification;
import com.DokkaiDorimu.entity.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User recipient, User sender, Article article, Comment comment, String message);
    List<NotificationDTO> getUnreadNotifications(User recipient);
    List<NotificationDTO> getNotificationsForUser(User user);
    void markAsRead(Long notificationId);
    void markAllAsRead(User recipient);
    long getUnreadCount(User recipient);
}
