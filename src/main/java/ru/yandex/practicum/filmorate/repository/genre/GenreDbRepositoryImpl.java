package ru.yandex.practicum.filmorate.repository.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
@Repository
@RequiredArgsConstructor
public class GenreDbRepositoryImpl implements GenreRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre saveGenre(Genre genre) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO GENRES (NAME)" +
                    " VALUES (?)", new String[]{"ID"});
            ps.setString(1, genre.getName());
            return ps;
        }, keyHolder);

        Long genreId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        genre.setId(genreId);

        return genre;
    }

    @Override
    public Genre getGenreById(Long id) {
        if (!existsGenreById(id)) {
            return null;
        }
        String sqlQuery = "SELECT * FROM GENRES WHERE ID = ?";
        return jdbcTemplate.queryForObject(sqlQuery, new GenreMapper(), id);
    }

    @Override
    public List<Genre> getAllGenres() {
        String sqlQuery = "SELECT * FROM GENRES ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, new GenreMapper());
    }

    @Override
    public Genre updateGenre(Genre genre) {
        if (!existsGenreById(genre.getId())) {
            return null;
        }
        String sqlQuery = "UPDATE GENRES " +
                "SET NAME = ? WHERE ID = ?";
        jdbcTemplate.update(sqlQuery, genre.getName(), genre.getId());
        return genre;
    }

    @Override
    public boolean existsGenreById(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM GENRES WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id.intValue()));
    }

    private static class GenreMapper implements RowMapper<Genre> {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Genre.builder()
                    .id(rs.getLong("ID"))
                    .name(rs.getString("NAME"))
                    .build();
        }
    }
}
