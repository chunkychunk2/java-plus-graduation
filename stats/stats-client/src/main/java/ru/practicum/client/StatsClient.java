package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate;
    private final LoadBalancerClient loadBalancer;

    @Value("${stats.server.name:STATS-SERVER}")
    private String statServerName;

    private final Random random = new Random();

    public void postHit(HitDto dto) {
        String url;

        try {
            url = getUrl();
        } catch (IllegalStateException e) {
            log.info("Stats service unavailable, skipping hit: {}", e.getMessage());
            return;
        }

        try {
            restTemplate.postForEntity(getUrl() + "/hit", dto, Void.class);
        } catch (RestClientException e) {
            log.info("Catch exception while trying to post hit");
        }
    }

    public List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String baseUrl;
        try {
            baseUrl = getUrl();
        } catch (IllegalStateException e) {
            log.info("Stats service unavailable, returning empty stats, {}", e.getMessage());
            return Collections.emptyList();
        }

        StringBuilder sb = new StringBuilder(baseUrl).append("/stats")
                .append("?start=").append(start)
                .append("&end=").append(end)
                .append("&unique=").append(unique);

        if (uris != null) {
            for (String uri : uris) {
                sb.append("&uris=").append(uri);
            }
        }

        ResponseEntity<StatsDto[]> response = null;

        try {
            response = restTemplate.getForEntity(sb.toString(), StatsDto[].class);
        } catch (RestClientException e) {
            log.info("Catch exception while trying to get stats");
        }

        StatsDto[] body = null;
        if (response != null) {
            body = response.getBody();
        }

        return (body == null) ? Collections.emptyList() : List.of(body);
    }

    private String getUrl() {
        try {
            ServiceInstance instance = loadBalancer.choose(statServerName);
            if (instance == null) {
                log.warn("No available instances for service: {}", statServerName);
                throw new IllegalStateException("Stats service unavailable");
            }
            return instance.getUri().toString();
        } catch (Exception e) {
            log.warn("Error getting stats service instance: {}", e.getMessage());
            throw new IllegalStateException("Stats service unavailable", e);
        }
    }
}