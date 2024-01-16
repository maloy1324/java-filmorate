package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        Film newFilm = filmService.addFilm(film);
        log.info("Фильм {} добавлен", film.getName());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм {} обновлен", film.getName());
        return updatedFilm;
    }

    @GetMapping
    public Collection<Film> findAllFilms() {
        return filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Film findFilm(@PathVariable Integer filmId) {
        return filmService.getFilm(filmId);
    }

}
