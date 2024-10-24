package com.DokkaiDorimu.DTO;

import com.DokkaiDorimu.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private String username;
    private Long userId;
    private Long parentCommentId;
    private String profileImageUrl;
    private String role;
    private List<CommentDTO> replies;

    public static CommentDTO fromComment(Comment comment, List<CommentDTO> replies) {
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getUser().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getUser().getImgUrl(),
                comment.getUser().getRole().name(),
                replies
        );
    }

    public static List<CommentDTO> fromCommentList(List<Comment> comments) {
        return comments.stream()
                .map(comment -> fromComment(comment, null))
                .collect(Collectors.toList());
    }
    public static String getSortField() {
        return "id"; // or any other field you want to use for sorting
    }
}