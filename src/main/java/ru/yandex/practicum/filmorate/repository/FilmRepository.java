package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FilmRepository {
    private int globalId = 0;
    private final Map<Integer, Film> films = new HashMap<>();

    public Film save(Film film) {
        film.setId(generateId());
        films.put(film.getId(), film);
        return film;
    }

    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        return null;
    }

    public Film getFilmById(int id) {
        return films.get(id);
    }

    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    public int generateId() {
        return ++globalId;
    }
}
