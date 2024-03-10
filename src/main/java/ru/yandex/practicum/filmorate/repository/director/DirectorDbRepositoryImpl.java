package ru.yandex.practicum.filmorate.repository.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("ALL")
@Component
@RequiredArgsConstructor
@Slf4j
public class DirectorDbRepositoryImpl implements DirectorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) { //создание режиссёра
        String sqlQuery = "INSERT INTO DIRECTORS (NAME) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        final Director dirDB = jdbcTemplate.queryForObject("SELECT * FROM DIRECTORS WHERE id = ?",
                new DirectorMapper(), id);
        log.debug("В базе создан режиссер: {}", dirDB);
        return dirDB;
    }

    @Override
    public List<Director> getDirectors() { //получение всех режиссёров
        return jdbcTemplate.query("SELECT * FROM DIRECTORS", new DirectorMapper());
    }

    @Override
    public Director getDirectorById(Long id) { //получение режиссёра по id
        return jdbcTemplate.queryForObject("SELECT * FROM DIRECTORS WHERE id = ?",
                new DirectorMapper(), id);
    }

    @Override
    public Director updateDirector(Director director) { //изменение режиссёра
        int count = jdbcTemplate.update("UPDATE DIRECTORS SET id = ?, name = ? WHERE id = ?",
                director.getId(), director.getName(), director.getId());
        if (count == 0) {
            throw new NotFoundException("Режиссёра с указанным id не существует", HttpStatus.NOT_FOUND);
        } else {
            log.info("В базе обновлен режиссер: {}", director);
            return getDirectorById(director.getId());
        }
    }

    @Override
    public void deleteDirectorById(Long id) { //удаление режиссёра
        final Director dir = getDirectorById(id);
        int count = jdbcTemplate.update("DELETE FROM DIRECTORS WHERE id = ?", id);
        if (count == 0) {
            throw new NotFoundException("Режиссёра с указанным id не существует", HttpStatus.NOT_FOUND);
        } else {
            log.info("В базе удален режиссер: {}", dir);
        }
    }

    @Override
    public LinkedHashSet<Director> getFilmDirectors(Long filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query("SELECT * FROM DIRECTORS WHERE id IN " +
                        "(SELECT DIRECTOR_ID FROM DIRECTOR_FILMS WHERE FILM_ID = ?)",
                new DirectorMapper(), filmId));
    }

//    @Override
//    public void addDirectorToFilm(int filmId, int directorId) {
//        String sqlQuery = "INSERT INTO DIRECTOR_FILMS (FILM_ID, director_id) values (?, ?)";
//        jdbcTemplate.update(sqlQuery, filmId, directorId);
//    }

    private static class DirectorMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Director(rs.getLong("id"), rs.getString("name"));
        }
    }
}