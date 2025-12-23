package ru.practicum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hit")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "app", nullable = false)
    String app;

    @Size(max = 255)
    @NotNull
    @Column(name = "uri", nullable = false)
    String uri;

    @Size(max = 255)
    @NotNull
    @Column(name = "ip", nullable = false)
    String ip;

    @NotNull
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

}