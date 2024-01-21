package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository {
    Film save(Film film);

    Film update(Film film);

    Film getFilmById(Long id);

    List<Film> findAll();

    boolean existsFilmById(Long filmId);

    List<Film> findPopularFilms(int count);
}
