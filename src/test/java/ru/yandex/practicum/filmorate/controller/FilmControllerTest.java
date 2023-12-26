package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.constant.FilmConstants;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmControllerTest {

    FilmController controller;

    FilmService service;

    FilmRepository repository;

    Film film = Film.builder()
            .id(1)
            .name("film name")
            .description("film description")
            .releaseDate(LocalDate.of(1997, 3, 24))
            .duration(100)
            .build();

    @BeforeEach
    void beforeEach() {
        repository = new FilmRepository();
        service = new FilmService(repository);
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
    void invalidName() {
        film.setName("");
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(film);
        });
        Assertions.assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void invalidDescription() {
        film.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                " Nulla a purus ullamcorper dolor commodo mattis non at ex." +
                " Vivamus scelerisque, ipsum non feugiat gravida, nulla augue tempor libero, eu aliquam.");
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(film);
        });
        assertEquals(String.format("Максимальная длина описания — %d символов",
                FilmConstants.MAX_DESCRIPTION_LENGTH), exception.getMessage());
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

    @Test
    void invalidDuration() {
        film.setDuration(-1);
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(film);
        });
        assertEquals("Продолжительность фильма должна быть положительной.", exception.getMessage());
    }
}