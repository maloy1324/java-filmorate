package ru.yandex.practicum.filmorate.constant;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class FilmConstants {
    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final String FILM_RELEASE_DATE_LIMIT = "1895-12-28";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String SIZE_OF_POPULAR_FILMS = "10";
}
