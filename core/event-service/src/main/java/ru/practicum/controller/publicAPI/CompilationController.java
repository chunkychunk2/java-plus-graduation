package ru.practicum.controller.publicAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.parameters.PageableSearchParam;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        PageableSearchParam param = PageableSearchParam.builder()
                .size(size)
                .from(from)
                .build();

        log.info("GET /compilations called");
        List<CompilationDto> compilations = compilationService.getAllCompilations(param.getPageable());
        log.info("Returned {} compilations", compilations.size());
        return compilations;
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("GET /compilations/{} called", compId);
        CompilationDto compilation = compilationService.getCompilationById(compId);
        log.info("Returned compilation: id={}, title={}", compilation.getId(), compilation.getTitle());
        return compilation;
    }
}
