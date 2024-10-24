package com.DokkaiDorimu.service;

import com.DokkaiDorimu.entity.ChatRoom;
import com.DokkaiDorimu.entity.Message;

import java.util.List;

public interface ChatService {
    Message sendMessage(Long senderId, Long recipientId, String content);
    List<Message> getConversation(Long user1Id, Long user2Id);
    ChatRoom createChatRoom(String name, List<Long> participantIds);
    Message sendMessageToChatRoom(Long senderId, Long chatRoomId, String content);
    List<Message> getChatRoomMessages(Long chatRoomId);
    List<ChatRoom> getUserChatRooms(Long userId);
}
