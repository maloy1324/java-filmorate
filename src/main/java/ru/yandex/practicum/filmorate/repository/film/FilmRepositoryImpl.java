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
    public Film saveFilm(Film film) {
        film.setId(generateId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        return null;
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        return films.get(filmId).getLikes().add(userId);
    }

    @Override
    public void deleteFilm(Long id) {
        films.remove(id);
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        return films.get(filmId).getLikes().remove(userId);
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean existsFilmById(Long filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Integer>> getUsersIDLikesIDSimilarTaste(Integer userId) {
        return null;
    }

    @Override
    public List<Film> getFilmsByFilmsId(List<Integer> filmsId) {
        return null;
    }

    private Long generateId() {
        return ++globalId;
    }
}
