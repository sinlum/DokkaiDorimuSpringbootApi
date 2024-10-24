package com.DokkaiDorimu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private boolean isRead;

//    // Add a method to safely get sender ID
//    public UserDTO getSenderId() {
//        return sender != null ? sender.getId() : null;
//    }
//    // Add a method to safely get recipient ID
//    public UserDTO getRecipientId() {
//        return recipient != null ? recipient.getId() : null;
//    }
}
