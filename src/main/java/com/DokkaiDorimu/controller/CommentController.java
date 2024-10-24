package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.DTO.CommentDTO;
import com.DokkaiDorimu.exception.CommentDeletionException;
import com.DokkaiDorimu.exception.ResourceNotFoundException;
import com.DokkaiDorimu.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{articleId}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long articleId,
                                                 @RequestBody Map<String, String> request) {
        String content = request.get("content");
        CommentDTO comment = commentService.addComment(articleId, null, content);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PostMapping("/{articleId}/comments/{parentCommentId}/replies")
    public ResponseEntity<CommentDTO> addReply(@PathVariable Long articleId,
                                               @PathVariable Long parentCommentId,
                                               @RequestBody Map<String, String> request) {
        String content = request.get("content");
        CommentDTO comment = commentService.addComment(articleId, parentCommentId, content);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{articleId}/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN') or @commentService.isCommentOwner(#commentId, authentication.name)")
    public ResponseEntity<?> deleteComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        try {
            commentService.deleteComment(articleId, commentId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException | CommentDeletionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the comment");
        }
    }

    @GetMapping("/{articleId}/comments")
    public ResponseEntity<Page<CommentDTO>> getCommentsByArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, CommentDTO.getSortField()));
        Page<CommentDTO> comments = commentService.getCommentsByArticle(articleId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comments/{parentId}/replies")
    public ResponseEntity<Page<CommentDTO>> getRepliesByParentComment(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, CommentDTO.getSortField()));
        Page<CommentDTO> replies = commentService.getRepliesByParentComment(parentId, pageable);
        return ResponseEntity.ok(replies);
    }
}