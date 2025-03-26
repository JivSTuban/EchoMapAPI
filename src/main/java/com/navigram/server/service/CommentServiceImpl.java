package com.navigram.server.service;

import com.navigram.server.dto.CommentDto;
import com.navigram.server.model.Comment;
import com.navigram.server.model.Memory;
import com.navigram.server.model.User;
import com.navigram.server.repository.CommentRepository;
import com.navigram.server.repository.MemoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final MemoryRepository memoryRepository;

    public CommentServiceImpl(CommentRepository commentRepository, MemoryRepository memoryRepository) {
        this.commentRepository = commentRepository;
        this.memoryRepository = memoryRepository;
    }

    @Override
    public CommentDto createComment(CommentDto commentDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Memory memory = memoryRepository.findById(commentDto.getMemoryId())
                .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setMemory(memory);
        comment.setUser(currentUser);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    @Override
    public List<CommentDto> getCommentsForMemory(String memoryId) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        return commentRepository.findByMemoryOrderByCreatedAtDesc(memory).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentById(String id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        return convertToDto(comment);
    }

    @Override
    public void deleteComment(String id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setMemoryId(comment.getMemory().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
