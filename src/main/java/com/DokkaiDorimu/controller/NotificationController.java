package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.DTO.NotificationDTO;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.UserNotFoundException;
import com.DokkaiDorimu.service.NotificationService;
import com.DokkaiDorimu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userService.getUserByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + currentUserEmail));
    }

    @MessageMapping("/notifications")
    @SendToUser("/topic/notifications")
    public List<NotificationDTO> handleWebSocketNotificationsRequest(Authentication authentication) {
        User currentUser = getCurrentUser();
        return notificationService.getNotificationsForUser(currentUser);
    }

    @MessageMapping("/notifications/unread-count")
    @SendToUser("/topic/notifications/count")
    public Map<String, Long> handleWebSocketUnreadCountRequest(Authentication authentication) {
        User currentUser = getCurrentUser();
        long count = notificationService.getUnreadCount(currentUser);
        return Map.of("count", count);
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        User currentUser = getCurrentUser();
        List<NotificationDTO> notificationDTOs = notificationService.getNotificationsForUser(currentUser);
        return ResponseEntity.ok(notificationDTOs);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        User currentUser = getCurrentUser();
        List<NotificationDTO> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(unreadNotifications);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = getCurrentUser();
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        User currentUser = getCurrentUser();
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok().build();
    }
}