package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.constant.FilmConstants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.time.LocalDate;
import java.util.Collection;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {

    private final FilmRepository repository;

    public Film addFilm(Film film) {
        Film validatedFilm = validate(film);

        if (repository.get(validatedFilm.getId()) == null) {
            film.setId(repository.generateId());
        }

        repository.save(film.getId(), film);
        log.info("Фильм добавлен.");
        return repository.get(film.getId());
    }

    public Film updateFilm(Film film) {
        if (repository.get(film.getId()) == null) {
            throw new NotFoundException("Фильм не найден.", NOT_FOUND);
        }
        repository.save(film.getId(), validate(film));
        log.info("Фильм обновлён.");
        return repository.get(film.getId());
    }

    public Film getFilm(Integer id) {
        Film film = repository.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.", NOT_FOUND);
        }
        return film;
    }

    public Collection<Film> findAll() {
        return repository.findAll();
    }

    private Film validate(Film film) {
        String filmName = film.getName();
        String filmDescription = film.getDescription();
        LocalDate filmReleaseDate = film.getReleaseDate();
        Integer filmDuration = film.getDuration();

        if (filmName == null || filmName.isBlank() || filmName.isEmpty()) {
            throw new ValidateException("Название не может быть пустым", BAD_REQUEST);
        }
        if (filmDescription.length() > FilmConstants.MAX_DESCRIPTION_LENGTH) {
            throw new ValidateException(
                    String.format("Максимальная длина описания — %d символов",
                            FilmConstants.MAX_DESCRIPTION_LENGTH), BAD_REQUEST);
        }
        if (filmReleaseDate.isBefore(FilmConstants.FILM_RELEASE_DATE_LIMIT)) {
            throw new ValidateException(
                    String.format(
                            "Дата релиза не может быть раньше %s",
                            FilmConstants.FILM_RELEASE_DATE_LIMIT.format(FilmConstants.DATE_FORMATTER)
                    ), BAD_REQUEST);
        }
        if (filmDuration < 0) {
            throw new ValidateException("Продолжительность фильма должна быть положительной.", BAD_REQUEST);
        }
        return film;
    }
}
