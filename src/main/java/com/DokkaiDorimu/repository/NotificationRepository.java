package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.Article;
import com.DokkaiDorimu.entity.Comment;
import com.DokkaiDorimu.entity.Notification;
import com.DokkaiDorimu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndIsReadFalse(User recipient);
    List<Notification> findByRecipientAndSenderAndArticleAndCommentAndIsReadFalse(
            User recipient, User sender, Article article, Comment comment);
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    long countByRecipientAndIsReadFalse(User recipient);
}