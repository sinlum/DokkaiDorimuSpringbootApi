package com.DokkaiDorimu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String publishedDate;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String genre;
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
