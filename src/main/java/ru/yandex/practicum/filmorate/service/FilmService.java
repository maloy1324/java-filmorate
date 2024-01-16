package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.constant.FilmConstants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.util.ValidationUtils;

import java.util.Collection;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {

    private final FilmRepository repository;

    private final ValidationUtils validator;

    public Film addFilm(Film film) {
        validator.validationRequest(film);
        return repository.save(validateReleaseDate(film));
    }

    public Film updateFilm(Film film) {
        validator.validationRequest(film);
        Film updatedFilm = repository.update(validateReleaseDate(film));
        if (updatedFilm == null) {
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        return updatedFilm;
    }

    public Film getFilm(Integer id) {
        Film film = repository.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        return film;
    }

    public Collection<Film> findAll() {
        return repository.findAll();
    }

    private Film validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FilmConstants.FILM_RELEASE_DATE_LIMIT)) {
            throw new ValidateException(
                    String.format(
                            "Дата релиза не может быть раньше %s",
                            FilmConstants.FILM_RELEASE_DATE_LIMIT.format(FilmConstants.DATE_FORMATTER)
                    ), BAD_REQUEST);
        }
        return film;
    }
}
