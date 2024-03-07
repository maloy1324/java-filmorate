package ru.yandex.practicum.filmorate.repository.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository(value = "filmDbRepositoryImpl")
public class FilmDbRepositoryImpl implements FilmRepository {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film saveFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)" +
                            " VALUES (?, ?, ?, ?, ?)",
                    new String[]{"ID"}
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId().intValue());
            return ps;
        }, keyHolder);

        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);
        insertFilmGenres(film);
        return getFilmById(filmId);
    }

    @Override
    public Film getFilmById(Long id) {
        if (!existsFilmById(id)) {
            return null;
        }
        String sqlQuery = "SELECT " +
                "f.*, M.NAME AS mpa_name, " +
                "GROUP_CONCAT(G2.ID ORDER BY G2.ID) AS GENRES_ID_LIST, " +
                "GROUP_CONCAT(G2.NAME ORDER BY G2.ID) AS genres_list, " +
                "(SELECT GROUP_CONCAT(USER_ID) FROM PUBLIC.FILMS_LIKES WHERE FILM_ID = f.ID) AS LIKES " +
                "FROM FILMS AS f " +
                "LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID " +
                "LEFT JOIN PUBLIC.FILMS_GENRES FG on f.ID = FG.FILM_ID " +
                "LEFT JOIN PUBLIC.GENRES G2 on G2.ID = FG.GENRE_ID " +
                "WHERE f.id = ? " +
                "GROUP BY f.ID";

        return jdbcTemplate.queryForObject(sqlQuery, new FilmMapper(), id);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*," +
                "       M.NAME                                                                AS mpa_name," +
                "       (SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST," +
                "       (SELECT GROUP_CONCAT(NAME)" +
                "        FROM GENRES" +
                "        WHERE ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS genres_list," +
                "       (SELECT GROUP_CONCAT(USER_ID)" +
                "        FROM PUBLIC.FILMS_LIKES" +
                "        WHERE FILM_ID = f.ID)                                                AS LIKES " +
                "FROM FILMS AS f" +
                "         LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID";
        return jdbcTemplate.query(sql, new FilmMapper());
    }

    @Override
    public Film updateFilm(Film film) {
        if (!existsFilmById(film.getId())) {
            return null;
        }
        jdbcTemplate.update("UPDATE FILMS" +
                        " SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? WHERE ID = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());


        jdbcTemplate.update("DELETE FROM FILMS_GENRES WHERE FILM_ID = ?", film.getId());

        insertFilmGenres(film);
        return getFilmById(film.getId());
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        String sqlQuery = "INSERT INTO FILMS_LIKES(FILM_ID, USER_ID) " +
                "SELECT ?, ? " +
                "FROM dual " +
                "WHERE NOT EXISTS(" +
                "    SELECT 1" +
                "    FROM FILMS_LIKES" +
                "    WHERE (FILM_ID = ? AND USER_ID = ?));";
        return jdbcTemplate.update(sqlQuery, filmId, userId, filmId, userId) > 0;
    }

    @Override
    public void deleteFilm(Long id) {
        jdbcTemplate.update("DELETE FROM FILMS WHERE ID = ?", id);
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        String sqlQuery = "DELETE FROM FILMS_LIKES WHERE FILM_ID = ? AND USER_ID = ?";
        return jdbcTemplate.update(sqlQuery, filmId, userId) > 0;
    }

    @Override
    public boolean existsFilmById(Long filmId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM FILMS WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, filmId.intValue()));
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        String sql = "SELECT f.*," +
                "       M.NAME                                                                AS mpa_name," +
                "       (SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST," +
                "       (SELECT GROUP_CONCAT(NAME)" +
                "        FROM GENRES" +
                "        WHERE ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS genres_list," +
                "       (SELECT GROUP_CONCAT(USER_ID)" +
                "        FROM PUBLIC.FILMS_LIKES" +
                "        WHERE FILM_ID = f.ID)                                                AS LIKES " +
                "FROM FILMS AS f" +
                "         LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID" +
                "         LEFT JOIN PUBLIC.FILMS_LIKES FL on F.ID = FL.FILM_ID " +
                "GROUP BY f.ID " +
                "ORDER BY COUNT(FL.USER_ID) DESC, F.ID " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, new FilmMapper(), count);
    }

    @Override
    public List<Film> findCommonFilms(Long userId, Long otherUserId) {
        String sqlForCommonFilmsId = "SELECT fl.FILM_ID " +
                "FROM FILMS_LIKES AS fl " +
                "WHERE fl.USER_ID = ? AND fl.FILM_ID IN (" +
                    "SELECT FILM_ID " +
                    "FROM FILMS_LIKES " +
                    "WHERE USER_ID = ?" +
                ")";
        String sql = "SELECT " +
                "f.*, " +
                "M.NAME AS mpa_name, " +
                "(SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                "(SELECT GROUP_CONCAT(NAME) FROM GENRES WHERE ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS genres_list, " +
                "(SELECT GROUP_CONCAT(USER_ID) FROM PUBLIC.FILMS_LIKES WHERE FILM_ID = f.ID) AS LIKES " +
                "FROM FILMS AS f " +
                "LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID " +
                "WHERE f.id IN (" + sqlForCommonFilmsId + ");";
        return jdbcTemplate.query(sql, new FilmMapper(), userId, otherUserId);
    }

    private void insertFilmGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            batchArgs.add(new Object[]{film.getId(), genre.getId()});
        }
        jdbcTemplate.batchUpdate("INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID) VALUES (?, ?)", batchArgs);
    }


    private static class FilmMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Film.builder()
                    .id(rs.getLong("ID"))
                    .name(rs.getString("NAME"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(getMpa(rs))
                    .genres(getGenres(rs))
                    .likes(getLikes(rs))
                    .build();
        }

        private Set<Long> getLikes(ResultSet rs) throws SQLException {
            Set<Long> likes = new HashSet<>();

            String likesIdList = rs.getString("LIKES");
            if (likesIdList != null) {
                String[] likeIds = likesIdList.split(",");

                for (String likeId : likeIds) {
                    likes.add(Long.parseLong(likeId));
                }
            }
            return likes;
        }

        private Set<Genre> getGenres(ResultSet rs) throws SQLException {
            Set<Genre> genres = new HashSet<>();

            String genresIdList = rs.getString("GENRES_ID_LIST");
            String genresList = rs.getString("genres_list");

            if (genresIdList != null && genresList != null) {
                String[] genreIds = genresIdList.split(",");
                String[] genreNames = genresList.split(",");

                for (int i = 0; i < genreIds.length; i++) {
                    genres.add(Genre.builder()
                            .id(Long.parseLong(genreIds[i]))
                            .name(genreNames[i])
                            .build());
                }
            }
            return genres;
        }

        private Mpa getMpa(ResultSet rs) throws SQLException {
            return Mpa.builder()
                    .id(rs.getLong("MPA_ID"))
                    .name(rs.getString("MPA_NAME"))
                    .build();
        }
    }
}
