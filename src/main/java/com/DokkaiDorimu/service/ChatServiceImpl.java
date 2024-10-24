package com.DokkaiDorimu.service;

import com.DokkaiDorimu.repository.ChatRoomRepository;
import com.DokkaiDorimu.repository.MessageRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.entity.ChatRoom;
import com.DokkaiDorimu.entity.Message;
import com.DokkaiDorimu.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    @Override
    public Message sendMessage(Long senderId, Long recipientId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        return messageRepository.save(message);
    }
    @Override
    public List<Message> getConversation(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElseThrow(() -> new RuntimeException("User1 not found"));
        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new RuntimeException("User2 not found"));

        return messageRepository.findBySenderAndRecipientOrderByTimestampAsc(user1, user2);
    }

    @Override
    public ChatRoom createChatRoom(String name, List<Long> participantIds) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);

        List<User> participants = userRepository.findAllById(participantIds);
        chatRoom.setParticipants(participants);

        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public Message sendMessageToChatRoom(Long senderId, Long chatRoomId, String content) {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new RuntimeException("Chat room not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    @Override
    public List<Message> getChatRoomMessages(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new RuntimeException("Chat room not found"));
        return messageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
    }

    @Override
    public List<ChatRoom> getUserChatRooms(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return chatRoomRepository.findByParticipantsContaining(user);
    }
}
