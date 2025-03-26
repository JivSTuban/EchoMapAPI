package com.navigram.server.repository;

import com.navigram.server.model.Comment;
import com.navigram.server.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByMemoryOrderByCreatedAtDesc(Memory memory);
}
