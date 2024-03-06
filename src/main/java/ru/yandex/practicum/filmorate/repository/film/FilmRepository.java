package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository {
    Film saveFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getAllFilms();

    Film updateFilm(Film film);

    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    void deleteFilm(Long id);

    boolean existsFilmById(Long filmId);

    List<Film> findPopularFilms(int count);

    List<Film> findCommonFilms(Long userId, Long otherUserId);
}
