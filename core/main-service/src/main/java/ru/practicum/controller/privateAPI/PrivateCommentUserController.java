package ru.practicum.controller.privateAPI;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentWithEventDto;
import ru.practicum.parameters.PageableSearchParam;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentUserController {

    private final CommentService service;

    @GetMapping
    public List<CommentWithEventDto> getUsersComment(@PathVariable @Positive Long userId,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                     @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting user's comments userId={}", userId);
        PageableSearchParam param = PageableSearchParam.builder()
                .from(from)
                .size(size)
                .build();
        return service.getUsersComments(userId, param.getPageable());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllUsersComments(@PathVariable @Positive Long userId) {
        log.info("Deleting user's comments userId={}", userId);
        service.deleteCommentsByUser(userId);
    }

}
