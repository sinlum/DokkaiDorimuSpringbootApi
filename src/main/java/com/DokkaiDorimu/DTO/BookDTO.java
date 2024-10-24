package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String publishedDate;
    private String description;
    private String genre;
    private String imageUrl;
    private String username;
    private String authorImageUrl;
}