package ru.yandex.practicum.filmorate.repository.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@Component
@RequiredArgsConstructor
@Slf4j
public class DirectorDbRepositoryImpl implements DirectorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) {
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
    public List<Director> getDirectors() {
        return jdbcTemplate.query("SELECT * FROM DIRECTORS", new DirectorMapper());
    }

    @Override
    public Director getDirectorById(Long id) {
        if (!existsDirectorById(id)) {
            throw new NotFoundException("Режиссёр не найден");
        }
        return jdbcTemplate.queryForObject("SELECT * FROM DIRECTORS WHERE id = ?",
                new DirectorMapper(), id);
    }

    @Override
    public Director updateDirector(Director director) {
        int count = jdbcTemplate.update("UPDATE DIRECTORS SET id = ?, name = ? WHERE id = ?",
                director.getId(), director.getName(), director.getId());
        if (count == 0) {
            throw new NotFoundException("Режиссёра с указанным id не существует");
        } else {
            log.info("В базе обновлен режиссер: {}", director);
            return getDirectorById(director.getId());
        }
    }

    @Override
    public void deleteDirectorById(Long id) {
        final Director dir = getDirectorById(id);
        int count = jdbcTemplate.update("DELETE FROM DIRECTORS WHERE id = ?", id);
        if (count == 0) {
            throw new NotFoundException("Режиссёра с указанным id не существует");
        } else {
            log.info("В базе удален режиссер: {}", dir);
        }
    }

    @Override
    public ArrayList<Director> getFilmDirectors(Long filmId) {
        return new ArrayList<>(jdbcTemplate.query("SELECT d.* FROM DIRECTORS d " +
                        "JOIN DIRECTOR_FILMS df ON d.id = df.DIRECTOR_ID " +
                        "WHERE df.FILM_ID = ?",
                new DirectorMapper(), filmId));
    }

    @Override
    public boolean existsDirectorById(Long directorId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM DIRECTORS WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, directorId.longValue()));
    }

    private static class DirectorMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Director(rs.getLong("id"), rs.getString("name"));
        }
    }
}