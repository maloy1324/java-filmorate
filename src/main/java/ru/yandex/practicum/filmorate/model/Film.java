package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.constant.FilmConstants;
import ru.yandex.practicum.filmorate.util.AfterDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static ru.yandex.practicum.filmorate.constant.FilmConstants.FILM_RELEASE_DATE_LIMIT;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Length(max = FilmConstants.MAX_DESCRIPTION_LENGTH, message = "Превышена максимальная длина описания")
    private String description;

    @AfterDate(value = FILM_RELEASE_DATE_LIMIT, message = "Дата релиза не может быть раньше 1985-12-28")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;

    private final Set<Long> likes = new HashSet<>();
}
