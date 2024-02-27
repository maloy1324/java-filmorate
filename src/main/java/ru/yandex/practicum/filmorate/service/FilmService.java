package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.Collection;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    @Autowired
    public FilmService(@Qualifier("filmDbRepositoryImpl") FilmRepository filmRepository,
                       @Qualifier("userDbRepositoryImpl") UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public Film addFilm(Film film) {
        return filmRepository.saveFilm(film);
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = filmRepository.updateFilm(film);
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

    public void addLike(Long id, Long userId) {
        checkId(id, userId);
        boolean isLiked = filmRepository.addLike(id, userId);
        if (!isLiked) {
            throw new ValidateException("Пользователь (ID :" + userId +
                    ") уже добавил фильм (ID:" + id + ") в понравишееся", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) добавил фильм (ID:{}) в понравишееся", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        checkId(id, userId);
        boolean likeRemoved = filmRepository.removeLike(id, userId);
        if (!likeRemoved) {
            throw new ValidateException("У пользователя (ID :" + userId +
                    ") нет фильма (ID:" + id + ") в понравишееся", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) удалил фильм (ID:{}) из понравившихся", userId, id);
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
        return filmRepository.getAllFilms();
    }

    private void checkId(Long id, Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        if (!filmRepository.existsFilmById(id)) {
            throw new NotFoundException("Фильма с ID: " + id + " не существует", NOT_FOUND);
        }
    }
}
