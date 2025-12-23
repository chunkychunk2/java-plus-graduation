package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.entity.Hit;
import ru.practicum.mapper.HitMapper;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void saveHit(HitDto hitDto) {
        log.info("Saving hit: {}", hitDto);
        Hit hit = hitMapper.toEntity(hitDto);
        hit.setCreatedAt(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER));
        hitRepository.save(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Getting stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        List<Object[]> rows;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                rows = hitRepository.getStatsUniqueIpWithoutUris(start, end);
            } else {
                rows = hitRepository.getStatsWithoutUris(start, end);
            }
        } else {
            if (unique) {
                rows = hitRepository.getStatsUniqueIp(start, end, uris);
            } else {
                rows = hitRepository.getStats(start, end, uris);
            }
        }

        return rows.stream().map(row -> new StatsDto((String) row[0],    // app
                (String) row[1],    // uri
                ((Number) row[2]).longValue() // hits
        )).collect(Collectors.toList());
    }
}
