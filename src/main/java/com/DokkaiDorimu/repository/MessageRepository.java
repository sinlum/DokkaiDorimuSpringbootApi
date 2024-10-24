package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.ChatRoom;
import com.DokkaiDorimu.entity.Message;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);
    List<Message> findBySenderAndRecipientOrderByTimestampAsc(User sender, User recipient);
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :user1Id AND m.recipient.id = :user2Id) OR (m.sender.id = :user2Id AND m.recipient.id = :user1Id) ORDER BY m.timestamp ASC")
    List<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    @Query("SELECT m.sender.id, COUNT(m) FROM Message m " +
            "WHERE m.recipient.id = :userId AND m.isRead = false " +
            "GROUP BY m.sender.id")
    List<Object[]> getUnreadMessageCountsByRecipient(@Param("userId") Long userId);
}
