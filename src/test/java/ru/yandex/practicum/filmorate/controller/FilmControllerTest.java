package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.film.FilmRepositoryImpl;
import ru.yandex.practicum.filmorate.repository.user.UserRepositoryImpl;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.util.ValidationUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FilmControllerTest {

    private FilmController controller;

    Film film = Film.builder()
            .id(1L)
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
        FilmRepositoryImpl filmRepository = new FilmRepositoryImpl();
        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        FilmService service = new FilmService(filmRepository, userRepository, new ValidationUtils(validator));
        controller = new FilmController(service);
    }

    @Test
    void create() {
        controller.createFilm(film);
        assertArrayEquals(List.of(film).toArray(), controller.findAllFilms().toArray());
    }

    @Test
    void update() {
        controller.createFilm(film);
        film.setName("new film name");
        controller.updateFilm(film);
        assertArrayEquals(List.of(film).toArray(), controller.findAllFilms().toArray());
    }
}