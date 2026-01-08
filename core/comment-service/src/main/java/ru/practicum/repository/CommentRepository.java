package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEvent(Long eventId, Pageable pageable);

    Page<Comment> findByAuthor(Long authorId, Pageable pageable);

    void deleteByAuthor(Long authorId);
}