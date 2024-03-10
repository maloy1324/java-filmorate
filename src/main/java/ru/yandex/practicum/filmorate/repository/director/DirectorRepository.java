package ru.yandex.practicum.filmorate.repository.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface DirectorRepository {

    Director createDirector(Director director);

    List<Director> getDirectors();

    Director getDirectorById(Long id);

    Director updateDirector(Director director);

    void deleteDirectorById(Long id);

    LinkedHashSet<Director> getFilmDirectors(Long filmId);
}
