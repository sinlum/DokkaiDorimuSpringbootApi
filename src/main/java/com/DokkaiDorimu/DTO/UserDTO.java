package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String currentPassword;
    private String imgUrl;
    private String status;
    private String address;
    private String bio;
    private String school;
    private String grade;
    private String role;
    private List<ArticleDTO> articles;
    private boolean isOnline;
    private int articleCount;
    private int totalViews;
    private int totalLikes;
    private int totalComments;
}