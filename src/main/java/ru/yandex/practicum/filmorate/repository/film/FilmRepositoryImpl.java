package ru.yandex.practicum.filmorate.repository.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class FilmRepositoryImpl implements FilmRepository {
    private Long globalId = 0L;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film save(Film film) {
        film.setId(generateId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        return null;
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean addLike(Long userId, Long filmId) {
        return films.get(filmId).getLikes().add(userId);
    }

    @Override
    public boolean existsFilmById(Long filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public boolean removeLike(Long userId, Long filmId) {
        return films.get(filmId).getLikes().remove(userId);
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private Long generateId() {
        return ++globalId;
    }
}
