package ru.yandex.practicum.filmorate.constant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilmConstants {
    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final LocalDate FILM_RELEASE_DATE_LIMIT = LocalDate.of(1895, 12, 28);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
