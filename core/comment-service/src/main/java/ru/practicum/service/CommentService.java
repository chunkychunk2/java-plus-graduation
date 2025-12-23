package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.EventClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.comment.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Comment;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.repository.CommentPreModerationRepository;
import ru.practicum.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final EventClient eventClient;
    private final UserClient userClient;
    private final CommentRepository commentRepository;
    private final CommentPreModerationRepository preModerationRepository;
    private final RequestClient requestClient;
    private final CommentMapper mapper;

    @Transactional
    @ClientErrorHandler
    public CommentDto addComment(Long userId, Long eventId, CreateUpdateCommentDto dto) {
        EventFullDto event = eventClient.getEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ForbiddenException("Событие должно быть опубликовано");
        }
        UserDto author = userClient.getUserById(userId);

        Set<String> forbiddenWords = preModerationRepository.forbiddenWordsByEventId(eventId);

        validateContent(dto.getContent(), forbiddenWords);

        Comment comment = new Comment();
        comment.setEvent(event.getId());
        comment.setAuthor(author.getId());
        comment.setContent(dto.getContent());
        comment.setCreated(LocalDateTime.now());
        comment.setUpdated(LocalDateTime.now());

        comment = commentRepository.save(comment);

        boolean isAuthorParticipant = requestClient.existsByRequesterAndEventAndStatus(userId, eventId,
                RequestStatus.CONFIRMED);

        CommentDto response = mapper.toDto(comment);
        response.setIsAuthorParticipant(isAuthorParticipant);
        return response;
    }


    @ClientErrorHandler
    @Transactional
    public void addPreModeration(Long userId, Long eventId, PreModerationRequest preModerationDto) {
        EventFullDto event = eventClient.getEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Только инициатор события может устанавливать премодерацию");
        }
        preModerationRepository.updateForbiddenWords(eventId, preModerationDto.getForbiddenWords());
    }

    @ClientErrorHandler
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CreateUpdateCommentDto dto) {
        EventFullDto event = eventClient.getEventById(eventId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!Objects.equals(event.getId(), comment.getEvent())) {
            throw new ConflictException("Собысте в пути запроса и событие комментария не совпадают");
        }
        if (comment.getAuthor() == null || !comment.getAuthor().equals(userId)) {
            throw new ForbiddenException("Редактировать можно только свои комментарии");
        }

        validateContent(dto.getContent(), preModerationRepository.forbiddenWordsByEventId(eventId));

        comment.setContent(dto.getContent());
        comment.setUpdated(LocalDateTime.now());

        comment = commentRepository.saveAndFlush(comment);

        boolean isAuthorParticipant = requestClient.existsByRequesterAndEventAndStatus(userId, eventId,
                RequestStatus.CONFIRMED);

        CommentDto response = mapper.toDto(comment);
        response.setIsAuthorParticipant(isAuthorParticipant);
        return response;
    }

    public List<CommentWithUserDto> getCommentsByEventId(Long eventId, Pageable pageable) {
        return commentRepository.findByEvent(eventId, pageable)
                .stream()
                .map(mapper::toWithUserDto)
                .toList();
    }

    public List<CommentWithEventDto> getUsersComments(Long userId, Pageable pageable) {
        return commentRepository.findByAuthor(userId, pageable)
                .stream()
                .map(mapper::toWithEventDto)
                .toList();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!comment.getAuthor().equals(userId)) {
            throw new ForbiddenException("Удалять можно только свои комментарии");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteCommentsByUser(Long userId) {
        commentRepository.deleteByAuthor(userId);
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