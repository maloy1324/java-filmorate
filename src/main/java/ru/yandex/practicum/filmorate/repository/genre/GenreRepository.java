package ru.yandex.practicum.filmorate.repository.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreRepository {
    Genre saveGenre(Genre genre);

    Genre getGenreById(Long id);

    List<Genre> getAllGenres();

    Genre updateGenre(Genre genre);

    boolean existsGenreById(Long id);
}
