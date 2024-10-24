package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.MessageDTO;
import com.DokkaiDorimu.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {
    List<MessageDTO> getMessagesBetweenUsers(Long user1Id, Long user2Id);
    Message markMessageAsRead(Long messageId, Long readerId);
    Message getMessageById(Long messageId);
    Message saveMessage(Message message);
    Map<Long, Long> getUnreadMessageCounts(Long userId);
}
