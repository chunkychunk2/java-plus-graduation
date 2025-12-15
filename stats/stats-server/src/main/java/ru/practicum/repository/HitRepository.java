package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Integer> {

    @Query("SELECT h.app, h.uri, COUNT(h) " + "FROM Hit h " + "WHERE h.createdAt BETWEEN :start AND :end " + "AND (:uris IS NULL OR h.uri IN :uris) " + "GROUP BY h.app, h.uri " + "ORDER BY COUNT(h) DESC")
    List<Object[]> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT h.app, h.uri, COUNT(DISTINCT h.ip) " + "FROM Hit h " + "WHERE h.createdAt BETWEEN :start AND :end " + "AND (:uris IS NULL OR h.uri IN :uris) " + "GROUP BY h.app, h.uri " + "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Object[]> getStatsUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT h.app, h.uri, COUNT(h) " + "FROM Hit h " + "WHERE h.createdAt BETWEEN :start AND :end " + "GROUP BY h.app, h.uri " + "ORDER BY COUNT(h) DESC")
    List<Object[]> getStatsWithoutUris(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h.app, h.uri, COUNT(DISTINCT h.ip) " + "FROM Hit h " + "WHERE h.createdAt BETWEEN :start AND :end " + "GROUP BY h.app, h.uri " + "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Object[]> getStatsUniqueIpWithoutUris(LocalDateTime start, LocalDateTime end);
}