package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class FilmRepository {
    private int globalId;
    private final Map<Integer, Film> films = new HashMap<>();

    public void save(int id, Film film) {
        films.put(id, film);
    }

    public Film get(int id) {
        return films.get(id);
    }

    public Collection<Film> findAll() {
        return films.values();
    }

    public int generateId() {
        return ++globalId;
    }
}
