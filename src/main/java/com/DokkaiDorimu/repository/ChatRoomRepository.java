package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.ChatRoom;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByParticipantsContaining(User user);
}
