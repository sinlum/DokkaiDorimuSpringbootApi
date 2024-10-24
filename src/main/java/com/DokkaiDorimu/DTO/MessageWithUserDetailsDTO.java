package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageWithUserDetailsDTO {
    private Long id;
    private UserDTO sender;
    private UserDTO recipient;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
}
