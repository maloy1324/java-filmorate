package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmRepository {
    Film saveFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getAllFilms();

    Film updateFilm(Film film);

    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    void deleteFilm(Long id);

    boolean existsFilmById(Long filmId);

    List<Film> findPopularFilms(int count, Long genreId, Long year);

    public List<Film> getAllFilmIfRequestParametersIsEmpty();

    List<Film> getAllFilmByRequestParameter(String query, String parameter);

    List<Film> findCommonFilms(Long userId, Long otherUserId);

    Map<Integer, List<Integer>> getUsersIDLikesIDSimilarTaste(Integer userId);

    List<Film> getFilmsByFilmsId(List<Integer> filmsId);

    List<Film> loadFilmsOfDirectorSortedByYears(Long directorId);

    List<Film> loadFilmsOfDirectorSortedByLikes(Long directorId);
}
