package ru.yandex.practicum.filmorate.repository.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;

public interface DirectorRepository {

    Director createDirector(Director director);

    List<Director> getDirectors();

    Director getDirectorById(Long id);

    Director updateDirector(Director director);

    void deleteDirectorById(Long id);

    ArrayList<Director> getFilmDirectors(Long filmId);

    boolean existsDirectorById(Long directorId);
}
