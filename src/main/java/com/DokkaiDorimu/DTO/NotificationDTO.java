package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long recipientId;
    private String recipientUsername;
    private Long senderId;
    private String senderUsername;
    private String senderImgUrl;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Long articleId;
    private Long commentId;
}
