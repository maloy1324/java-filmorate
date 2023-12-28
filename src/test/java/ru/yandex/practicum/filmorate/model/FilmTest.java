package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmTest {
    private Validator validator;
    private final Film film = Film.builder()
            .id(1)
            .name("film name")
            .description("film description")
            .releaseDate(LocalDate.of(1998, 7, 20))
            .duration(100)
            .build();

    @BeforeEach
    void setUp() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @Test
    void invalidName() {
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void invalidDescription() {
        film.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                " Vestibulum id neque laoreet, porta ligula ac, dignissim mi." +
                " Proin elementum ipsum at rhoncus faucibus. Mauris in nisi vitae purus ultricies non.");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("Превышена максимальная длина описания", violations.iterator().next().getMessage());
    }

    @Test
    void invalidDuration() {
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals("Продолжительность фильма должна быть положительной",
                violations.iterator().next().getMessage());
    }
}