package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.genre.GenreRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public Genre saveGenre(Genre genre) {
        return genreRepository.saveGenre(genre);
    }

    public List<Genre> getAllGenres() {
        return genreRepository.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        Genre genre = genreRepository.getGenreById(id);
        if (genre == null) {
            throw new NotFoundException("Жанр не найден.");
        }
        return genre;
    }

    public Genre updateGenre(Genre genre) {
        Genre updatedGenre = genreRepository.updateGenre(genre);
        if (updatedGenre == null) {
            throw new NotFoundException("Жанр не найден.");
        }
        return updatedGenre;
    }
}
