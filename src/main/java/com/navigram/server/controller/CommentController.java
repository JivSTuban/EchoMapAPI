package com.navigram.server.controller;

import com.navigram.server.dto.CommentDto;
import com.navigram.server.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto) {
        return new ResponseEntity<>(commentService.createComment(commentDto), HttpStatus.CREATED);
    }

    @GetMapping("/memory/{memoryId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommentDto>> getCommentsForMemory(@PathVariable String memoryId) {
        return ResponseEntity.ok(commentService.getCommentsForMemory(memoryId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and (authentication.principal.username == @commentService.getCommentById(#id).user.username or hasRole('ADMIN'))")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}