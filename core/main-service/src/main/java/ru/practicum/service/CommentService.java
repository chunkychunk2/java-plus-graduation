package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.*;
import ru.practicum.entity.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ParticipationRequestRepository requestRepository;
    private final CommentMapper mapper;

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CreateUpdateCommentDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ForbiddenException("Событие должно быть опубликовано");
        }
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        validateContent(dto.getContent(), event.getForbiddenWords());

        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setContent(dto.getContent());
        comment.setCreated(LocalDateTime.now());
        comment.setUpdated(LocalDateTime.now());

        comment = commentRepository.save(comment);

        boolean isAuthorParticipant = requestRepository.existsByRequester_IdAndEvent_IdAndStatus(userId, eventId,
                RequestStatus.CONFIRMED);

        CommentDto response = mapper.toDto(comment);
        response.setIsAuthorParticipant(isAuthorParticipant);
        return response;
    }


    @Transactional
    public void addPreModeration(Long userId, Long eventId, PreModerationRequest preModerationDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Только инициатор события может устанавливать премодерацию");
        }
        if (event.getForbiddenWords() == null) {
            event.setForbiddenWords(preModerationDto.getForbiddenWords());
        } else {
            event.getForbiddenWords().addAll(preModerationDto.getForbiddenWords());
        }
        eventRepository.save(event);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CreateUpdateCommentDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!Objects.equals(event.getId(), comment.getEvent().getId())) {
            throw new ConflictException("Собысте в пути запроса и событие комментария не совпадают");
        }
        if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать можно только свои комментарии");
        }

        validateContent(dto.getContent(), comment.getEvent().getForbiddenWords());

        comment.setContent(dto.getContent());
        comment.setUpdated(LocalDateTime.now());

        comment = commentRepository.saveAndFlush(comment);

        boolean isAuthorParticipant = requestRepository.existsByRequester_IdAndEvent_IdAndStatus(userId, eventId,
                RequestStatus.CONFIRMED);

        CommentDto response = mapper.toDto(comment);
        response.setIsAuthorParticipant(isAuthorParticipant);
        return response;
    }

    public List<CommentWithUserDto> getCommentsByEventId(Long eventId, Pageable pageable) {
        return commentRepository.findByEventId(eventId, pageable)
                .stream()
                .map(mapper::toWithUserDto)
                .toList();
    }

    public List<CommentWithEventDto> getUsersComments(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorId(userId, pageable)
                .stream()
                .map(mapper::toWithEventDto)
                .toList();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Удалять можно только свои комментарии");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteCommentsByUser(Long userId) {
        commentRepository.deleteByAuthorId(userId);
    }

    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        commentRepository.delete(comment);
    }

    private static void validateContent(String content, Set<String> forbiddenWords) {
        boolean hasForbidden = Arrays.stream(content.split(" ")).anyMatch(forbiddenWords::contains);
        if (hasForbidden) {
            throw new ForbiddenException("Комментарий содержит запрещённые слова");
        }
    }
}