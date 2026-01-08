package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentWithUserDto;
import ru.practicum.parameters.PageableSearchParam;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/comment")
@RequiredArgsConstructor
public class CommentClientController {

    private final CommentService commentService;

    @GetMapping("/{eventId}")
    public List<CommentWithUserDto> getCommentsByEventId(@PathVariable @Positive Long eventId,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        PageableSearchParam param = PageableSearchParam.builder().size(size).from(from).build();
        List<CommentWithUserDto> commentsByEventId = commentService.getCommentsByEventId(eventId, param.getPageable());
        log.info("Returned {} comments to event id={}", commentsByEventId.size(), eventId);
        return commentsByEventId;
    }
}
