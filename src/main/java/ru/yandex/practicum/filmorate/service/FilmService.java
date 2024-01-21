package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.film.FilmRepositoryImpl;
import ru.yandex.practicum.filmorate.repository.user.UserRepositoryImpl;

import java.util.Collection;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {

    private final FilmRepositoryImpl filmRepository;
    private final UserRepositoryImpl userRepository;

    public Film addFilm(Film film) {
        return filmRepository.save(film);
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = filmRepository.update(film);
        if (updatedFilm == null) {
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        return updatedFilm;
    }

    public Film getFilm(Long id) {
        Film film = filmRepository.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        return film;
    }

    public void addLike(Long userId, Long filmId) {
        checkId(userId, filmId);
        boolean isLiked = filmRepository.getFilmById(filmId).getLikes().add(userId);
        if (!isLiked) {
            throw new ValidateException("Пользователь (ID :" + userId +
                    ") уже добавил фильм (ID:" + filmId + ") в понравишееся", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) добавил фильм (ID:{}) в понравишееся", userId, filmId);
    }

    public void removeLike(Long userId, Long filmId) {
        checkId(userId, filmId);
        boolean likeRemoved = filmRepository.getFilmById(filmId).getLikes().remove(userId);
        if (!likeRemoved) {
            throw new ValidateException("У пользователя (ID :" + userId +
                    ") нет фильма (ID:" + filmId + ") в понравишееся", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) удалил фильм (ID:{}) из понравившихся", userId, filmId);
    }

    public List<Film> findPopularFilms(String count) {
        try {
            int size = Integer.parseInt(count);
            log.info("Список популярных фильмов отправлен");
            return filmRepository.findPopularFilms(size);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll();
    }

    private void checkId(Long userId, Long filmId) {
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        if (!filmRepository.existsFilmById(filmId)) {
            throw new NotFoundException("Фильма с ID: " + filmId + " не существует", NOT_FOUND);
        }
    }
}
