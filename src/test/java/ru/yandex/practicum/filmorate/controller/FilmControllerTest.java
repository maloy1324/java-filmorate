package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.constant.FilmConstants;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.util.ValidationUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmControllerTest {

    private FilmController controller;

    Film film = Film.builder()
            .id(1)
            .name("film name")
            .description("film description")
            .releaseDate(LocalDate.of(1997, 3, 24))
            .duration(100)
            .build();

    @BeforeEach
    void beforeEach() {
        Validator validator;
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
        FilmRepository repository = new FilmRepository();
        FilmService service = new FilmService(repository, new ValidationUtils(validator));
        controller = new FilmController(service);
    }

    @Test
    void create() {
        controller.create(film);
        assertArrayEquals(List.of(film).toArray(), controller.findAll().toArray());
    }

    @Test
    void update() {
        controller.create(film);
        film.setName("new film name");
        controller.update(film);
        assertArrayEquals(List.of(film).toArray(), controller.findAll().toArray());
    }

    @Test
    void invalidReleaseDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(film);
        });
        assertEquals(String.format(
                "Дата релиза не может быть раньше %s",
                FilmConstants.FILM_RELEASE_DATE_LIMIT.format(FilmConstants.DATE_FORMATTER)
        ), exception.getMessage());
    }
}