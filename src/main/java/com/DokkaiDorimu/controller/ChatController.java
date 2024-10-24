package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.DTO.MessageDTO;
import com.DokkaiDorimu.DTO.ReadReceiptDTO;
import com.DokkaiDorimu.DTO.UserDTO;
import com.DokkaiDorimu.entity.Message;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.service.ChatService;
import com.DokkaiDorimu.service.MessageService;
import com.DokkaiDorimu.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO) {
        try {
            System.out.println("Received message DTO: " + messageDTO);

            if (messageDTO.getSender() == null) {
                throw new IllegalArgumentException("Sender is required");
            }
            if (messageDTO.getRecipient() == null) {
                throw new IllegalArgumentException("Recipient is required");
            }
            if (messageDTO.getSender().getId() == null) {
                throw new IllegalArgumentException("Sender ID is required");
            }
            if (messageDTO.getRecipient().getId() == null) {
                throw new IllegalArgumentException("Recipient ID is required");
            }

            User sender = userService.getUserById(messageDTO.getSender().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
            User recipient = userService.getUserById(messageDTO.getRecipient().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

            Message savedMessage = chatService.sendMessage(sender.getId(), recipient.getId(), messageDTO.getContent());
            MessageDTO savedMessageDTO = convertToDTO(savedMessage);

            messagingTemplate.convertAndSendToUser(
                    recipient.getId().toString(),
                    "/queue/messages",
                    savedMessageDTO
            );
            System.out.println("Message sent successfully: " + savedMessageDTO);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.sendMessageToChatRoom")
    public void sendMessageToChatRoom(@Payload Message message) {
        Message savedMessage = chatService.sendMessageToChatRoom(message.getSender().getId(), message.getChatRoom().getId(), message.getContent());
        messagingTemplate.convertAndSend(
                "/topic/chatroom." + message.getChatRoom().getId(),
                savedMessage
        );
    }
    @GetMapping("/api/messages/{userId}")
    public ResponseEntity<List<MessageDTO>> getMessagesBetweenUsers(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String currentUserEmail = userDetails.getUsername(); // This is actually the email
            System.out.println("Attempting to fetch user with email: " + currentUserEmail);

            User currentUser = userService.getUserByEmail(currentUserEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Current user not found for email: " + currentUserEmail));

            System.out.println("Current user found: " + currentUser.getId());

            List<MessageDTO> messages = messageService.getMessagesBetweenUsers(currentUser.getId(), userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error fetching messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/api/messages/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String currentUserEmail = userDetails.getUsername(); // This is actually the email
            User currentUser = userService.getUserByEmail(currentUserEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + currentUserEmail));

            Message updatedMessage = messageService.markMessageAsRead(messageId, currentUser.getId());
            return ResponseEntity.ok(updatedMessage);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }
    @GetMapping("/api/messages/unread-counts")
    public ResponseEntity<Map<Long, Long>> getUnreadMessageCounts(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String currentUserEmail = userDetails.getUsername(); // This is actually the email
            User currentUser = userService.getUserByEmail(currentUserEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + currentUserEmail));

            Map<Long, Long> unreadCounts = messageService.getUnreadMessageCounts(currentUser.getId());
            return ResponseEntity.ok(unreadCounts);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.emptyMap());
        }
    }
    @MessageMapping("/chat.messageRead")
    public void handleMessageRead(@Payload ReadReceiptDTO readReceipt) {
        Message message = messageService.getMessageById(readReceipt.getMessageId());
        if (message != null && message.getRecipient().getId().equals(readReceipt.getReaderId())) {
            message.setRead(true);
            Message updatedMessage = messageService.saveMessage(message);

            // Notify the sender that their message has been read
            messagingTemplate.convertAndSendToUser(
                    message.getSender().getId().toString(),
                    "/queue/messageRead",
                    updatedMessage
            );
        }
    }

//    @MessageMapping("/user.online")
//    public void userOnline(Principal principal) {
//        logger.info("Received online status update request for user: {}", principal.getName());
//
//        userService.getUserByUsername(principal.getName()).ifPresentOrElse(
//                user -> {
//                    logger.info("Updating online status for user: {} (ID: {})", user.getUsername(), user.getId());
//                    userService.setUserOnlineStatus(user.getId(), true);
//                    logger.info("Broadcasting online status for user ID: {}", user.getId());
//                    messagingTemplate.convertAndSend("/topic/user.online", user.getId());
//                    logger.info("Online status update completed for user: {}", user.getUsername());
//                },
//                () -> logger.warn("User not found for username: {}", principal.getName())
//        );
//    }

//    @MessageMapping("/user.offline")
//    public void userOffline(Principal principal) {
//        logger.info("Received offline status update request for user: {}", principal.getName());
//
//        userService.getUserByUsername(principal.getName()).ifPresentOrElse(
//                user -> {
//                    logger.info("Updating offline status for user: {} (ID: {})", user.getUsername(), user.getId());
//                    userService.updateOnlineStatus(user.getId(), false);
//                    logger.info("Broadcasting offline status for user ID: {}", user.getId());
//                    messagingTemplate.convertAndSend("/topic/user.offline", user.getId());
//                    logger.info("Offline status update completed for user: {}", user.getUsername());
//                },
//                () -> logger.warn("User not found for username: {}", principal.getName())
//        );
//    }

    public static MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSender(convertToUserDTO(message.getSender()));
        dto.setRecipient(convertToUserDTO(message.getRecipient()));
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        return dto;
    }
    public static UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole().toString());
        // Set other necessary fields, but avoid collections
        return dto;
    }

}
