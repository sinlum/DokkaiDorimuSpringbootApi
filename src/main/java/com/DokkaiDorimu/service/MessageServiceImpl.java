package com.DokkaiDorimu.service;

import com.DokkaiDorimu.controller.ChatController;
import com.DokkaiDorimu.repository.MessageRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.DTO.MessageDTO;
import com.DokkaiDorimu.DTO.UserDTO;
import com.DokkaiDorimu.entity.Message;
import com.DokkaiDorimu.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
    @Override
    public List<MessageDTO> getMessagesBetweenUsers(Long user1Id, Long user2Id) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1Id, user2Id);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
//    @Transactional
//    public Message markMessageAsRead(Long messageId, Long readerId) {
//        Message message = getMessageById(messageId);
//        User reader = userRepository.findById(readerId)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        if (message.getRecipient().getId().equals(readerId)) {
//            message.setRead(true);
//            return saveMessage(message);
//        } else {
//            throw new IllegalArgumentException("Only the recipient can mark the message as read");
//        }
//    }
//    public Map<Long, Long> getUnreadMessageCounts(Long userId) {
//        List<Object[]> results = messageRepository.getUnreadMessageCountsByRecipient(userId);
//        Map<Long, Long> unreadCounts = new HashMap<>();
//        for (Object[] result : results) {
//            Long senderId = (Long) result[0];
//            Long count = (Long) result[1];
//            unreadCounts.put(senderId, count);
//        }
//        return unreadCounts;
//    }
    @Transactional
    public Message markMessageAsRead(Long messageId, Long readerId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));

    // We don't need to fetch the reader if we're not using it
        if (Objects.equals(message.getRecipient().getId(), readerId)) {
            message.setRead(true);
            return messageRepository.save(message);
        } else {
            throw new IllegalArgumentException("Only the recipient can mark the message as read");
        }
    }
    public Map<Long, Long> getUnreadMessageCounts(Long userId) {
        return messageRepository.getUnreadMessageCountsByRecipient(userId).stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1],
                        (v1, v2) -> v1, // In case of duplicate keys, keep the first value
                        HashMap::new
                ));
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());

        // Use the safe methods we added to the Message entity
        dto.setSender(ChatController.convertToUserDTO(message.getSender()));
        dto.setRecipient(ChatController.convertToUserDTO(message.getRecipient()));

        return dto;
    }
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImgUrl(user.getImgUrl());
        dto.setStatus(user.getStatus());
        dto.setAddress(user.getAddress());
        dto.setBio(user.getBio());
        dto.setRole(user.getRole().name());
        // Don't set the password in DTO for security reasons
        return dto;
    }
}
