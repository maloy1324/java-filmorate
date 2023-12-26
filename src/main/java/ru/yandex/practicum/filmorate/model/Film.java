package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
}
