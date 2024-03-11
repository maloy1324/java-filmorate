package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static ru.yandex.practicum.filmorate.constant.Constants.DEFAULT_COUNT;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@RequestBody @Valid Film film) {
        Film newFilm = filmService.addFilm(film);
        log.info("Фильм {} добавлен", film.getName());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм {} обновлен", film.getName());
        return updatedFilm;
    }

    @GetMapping
    public Collection<Film> findAllFilms() {
        return filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Film findFilm(@PathVariable Long filmId) {
        return filmService.getFilm(filmId);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable Long filmId) {
        filmService.deleteFilm(filmId);
        log.info("Фильм c ID {} удалён.", filmId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> findPopularFilms(@RequestParam(defaultValue = DEFAULT_COUNT) String count) {
        return filmService.findPopularFilms(count);
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam(value = "query", required = false) String query, String by) {
        log.info("Поиск фильмов по параметрам запроса. Текст запроса: " + query + ", поле запроса: " + by);
        return filmService.search(query, by);
    }

    @GetMapping("/common")
    public List<Film> findCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.findCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilmsByDirectorId(@PathVariable Long directorId, @RequestParam String sortBy) {
        return filmService.getSortedFilmsByDirectorId(directorId, sortBy);
    }
}
