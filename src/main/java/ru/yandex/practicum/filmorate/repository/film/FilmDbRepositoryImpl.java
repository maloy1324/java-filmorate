package ru.yandex.practicum.filmorate.repository.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("ALL")
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
        insertFilmDirector(film);
        return getFilmById(filmId);
    }

    @Override
    public Film getFilmById(Long id) {
        if (!existsFilmById(id)) {
            return null;
        }
        String sqlQuery = "SELECT f.*, " +
                "       M.NAME                                                                      AS MPA_NAME, " +
                "       GROUP_CONCAT(G2.ID ORDER BY G2.ID)                                          AS GENRES_ID_LIST, " +
                "       GROUP_CONCAT(G2.NAME ORDER BY G2.ID)                                        AS genres_list, " +
                "       (SELECT GROUP_CONCAT(USER_ID) FROM PUBLIC.FILMS_LIKES WHERE FILM_ID = f.ID) AS LIKES, " +
                "       GROUP_CONCAT(D.DIRECTOR_ID ORDER BY D.DIRECTOR_ID)                          AS DIRECTOR_ID_LIST, " +
                "       GROUP_CONCAT(D1.NAME ORDER BY D1.ID)                                        AS DIRECTORS_LIST " +
                "FROM FILMS AS f " +
                "         LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID " +
                "         LEFT JOIN PUBLIC.FILMS_GENRES FG on f.ID = FG.FILM_ID " +
                "         LEFT JOIN PUBLIC.GENRES G2 on G2.ID = FG.GENRE_ID " +
                "         LEFT JOIN (SELECT FILM_ID, DIRECTOR_ID " +
                "      FROM PUBLIC.DIRECTOR_FILMS) AS D ON f.ID = D.FILM_ID " +
                "         LEFT JOIN PUBLIC.DIRECTORS D1 on D1.ID = D.DIRECTOR_ID " +
                "WHERE f.id = ? " +
                "GROUP BY f.ID";

        return jdbcTemplate.queryForObject(sqlQuery, new FilmMapper(), id);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, " +
                "       M.NAME                                                                                   AS MPA_NAME, " +
                "       (SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                "       (SELECT GROUP_CONCAT(DIRECTOR_ID ORDER BY DIRECTOR_ID) FROM DIRECTOR_FILMS WHERE FILM_ID = f.id) AS DIRECTOR_ID_LIST, " +
                "       (SELECT GROUP_CONCAT(NAME) " +
                "        FROM GENRES " +
                "        WHERE ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id))                   AS genres_list, " +
                "       (SELECT GROUP_CONCAT(NAME) " +
                "        FROM DIRECTORS " +
                "        WHERE ID IN (SELECT DIRECTOR_ID FROM DIRECTOR_FILMS WHERE FILM_ID = f.id))                   AS DIRECTORS_LIST, " +
                "       (SELECT GROUP_CONCAT(USER_ID) " +
                "        FROM PUBLIC.FILMS_LIKES " +
                "        WHERE FILM_ID = f.ID)                                                                   AS LIKES " +
                "FROM FILMS AS f " +
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
        film.getDirectors();


        jdbcTemplate.update("DELETE FROM FILMS_GENRES WHERE FILM_ID = ?", film.getId());
        jdbcTemplate.update("DELETE FROM DIRECTOR_FILMS WHERE FILM_ID = ?", film.getId());

        insertFilmGenres(film);
        insertFilmDirector(film);
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
    public List<Film> findPopularFilms(int count, Long genreId, Long year) {
        String sql = "SELECT f.*, M.NAME AS mpa_name, " +
                "(SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES " +
                "WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                "(SELECT GROUP_CONCAT(g.NAME) FROM GENRES g " +
                "WHERE g.ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS genres_list, " +
                "(SELECT GROUP_CONCAT(USER_ID) FROM PUBLIC.FILMS_LIKES WHERE FILM_ID = f.ID) AS LIKES, " +
                "(SELECT GROUP_CONCAT(DIRECTOR_ID) " +
                "FROM DIRECTOR_FILMS fd where FILM_ID = f.ID) as DIRECTOR_ID_LIST, " +
                "(SELECT GROUP_CONCAT(d.NAME) FROM directors d where d.ID in " +
                "(SELECT DIRECTOR_ID FROM DIRECTOR_FILMS fd where FILM_ID = f.ID)) as DIRECTORS_LIST " +
                "FROM FILMS AS f " +
                "LEFT JOIN PUBLIC.MPA M ON M.ID = f.MPA_ID " +
                "LEFT JOIN PUBLIC.FILMS_LIKES FL ON f.ID = FL.FILM_ID " +
                "WHERE (? IS NULL OR YEAR(f.RELEASE_DATE) = ?) " + // фильтр по году, если указан
                "AND (? IS NULL OR f.ID IN (SELECT FILM_ID FROM FILMS_GENRES WHERE GENRE_ID = ?)) " + // фильтр по жанру, если указан
                "GROUP BY f.ID " +
                "ORDER BY COUNT(FL.USER_ID) DESC, f.ID " +
                "LIMIT ?"; // лимит на кол-во фильмов
        return jdbcTemplate.query(sql, new FilmMapper(), year, year, genreId, genreId, count);
    }

    @Override
    public List<Film> getAllFilmByRequestParameter(String query, String parameter1, String parameter2) {
        String sql = "SELECT f.*," +
                "     m.NAME AS mpa_name," +
                "     (SELECT STRING_AGG(GENRE_ID::CHARACTER VARYING, ',' ORDER BY GENRE_ID) FROM FILMS_GENRES " +
                "     WHERE FILM_ID = f.ID) AS GENRES_ID_LIST," +
                "     (SELECT STRING_AGG(NAME, ',') FROM GENRES g WHERE ID IN " +
                "     (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.ID)) AS GENRES_LIST," +
                "     (SELECT string_agg(USER_ID::CHARACTER VARYING, ',') " +
                "     FROM PUBLIC.FILMS_LIKES " +
                "     WHERE FILM_ID = f.ID) AS LIKES " +
                "FROM FILMS f " +
                "LEFT JOIN MPA m ON m.ID = f.MPA_ID " +
                "LEFT JOIN PUBLIC.FILMS_LIKES FL on f.ID = FL.FILM_ID " +
                "WHERE ? IS NOT NULL AND f.NAME ILIKE ?" +
                "GROUP BY f.ID, mpa_name " +
                "ORDER BY COUNT(FL.USER_ID) DESC, f.ID";
        return jdbcTemplate.query(sql, new FilmMapper(), parameter1, query);
    }

    @Override
    public List<Film> getAllFilmIfRequestParametersIsEmpty() {
        String sql = "SELECT f.*," +
                "     m.NAME as mpa_name," +
                "     (SELECT STRING_AGG(GENRE_ID::CHARACTER VARYING, ',' ORDER BY GENRE_ID) FROM FILMS_GENRES" +
                "     WHERE FILM_ID = f.ID) AS GENRES_ID_LIST," +
                "     (SELECT STRING_AGG(NAME, ',') FROM GENRES WHERE ID IN " +
                "     (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.ID)) AS GENRES_LIST," +
                "     (SELECT STRING_AGG(USER_ID::CHARACTER VARYING, ',') " +
                "     FROM PUBLIC.FILMS_LIKES " +
                "     WHERE FILM_ID = f.ID) AS LIKES " +
                "FROM FILMS f " +
                "LEFT JOIN MPA m ON m.ID = f.MPA_ID " +
                "LEFT JOIN PUBLIC.FILMS_LIKES FL ON f.ID = FL.FILM_ID " +
                "GROUP BY f.ID, mpa_name " +
                "ORDER BY COUNT(FL.USER_ID) DESC, f.ID";
        return jdbcTemplate.query(sql, new FilmMapper());
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
                "M.NAME AS MPA_NAME, " +
                "(SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                "(SELECT GROUP_CONCAT(NAME) FROM GENRES WHERE ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS genres_list, " +
                "(SELECT GROUP_CONCAT(USER_ID) FROM PUBLIC.FILMS_LIKES WHERE FILM_ID = f.ID) AS LIKES, " +
                "(SELECT GROUP_CONCAT(DIRECTOR_ID) " +
                "FROM DIRECTOR_FILMS fd where FILM_ID = f.ID) as DIRECTOR_ID_LIST, " +
                "(SELECT GROUP_CONCAT(NAME) FROM directors d where ID in " +
                "(SELECT DIRECTOR_ID FROM DIRECTOR_FILMS fd where FILM_ID = f.ID)) as DIRECTORS_LIST " +
                "FROM FILMS AS f " +
                "LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID " +
                "WHERE f.id IN (" + sqlForCommonFilmsId + ");";
        return jdbcTemplate.query(sql, new FilmMapper(), userId, otherUserId);
    }

    @Override
    public Map<Integer, List<Integer>> getUsersIDLikesIDSimilarTaste(Integer userId) {
        // Запрос на лайки пользователя.
        String sqlUserLikes = "SELECT fl.FILM_ID " +
                "FROM FILMS_LIKES AS fl " +
                "WHERE fl.USER_ID = ?";
        // Запрос на id пользователей с пересечением лайков с целевым пользователем.
        String sqlUsersIdSimilarTaste = "SELECT USER_ID " +
                "FROM FILMS_LIKES " +
                "WHERE FILM_ID IN (" + sqlUserLikes + ")";
        String sql = "SELECT FILM_ID, USER_ID " +
                "FROM FILMS_LIKES " +
                "WHERE USER_ID IN (" + sqlUsersIdSimilarTaste + ");";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId);
        return getUsersLikes(rowSet);
    }

    @Override
    public List<Film> getFilmsByFilmsId(List<Integer> filmsId) {
        String inSql = String.join(",", Collections.nCopies(filmsId.size(), "?"));
        String sql = "SELECT " +
                "f.*, " +
                "M.NAME AS MPA_NAME, " +
                "(SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                "(SELECT GROUP_CONCAT(NAME) FROM GENRES WHERE ID IN (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS genres_list, " +
                "(SELECT GROUP_CONCAT(USER_ID) FROM PUBLIC.FILMS_LIKES WHERE FILM_ID = f.ID) AS LIKES, " +
                "(SELECT GROUP_CONCAT(DIRECTOR_ID) " +
                "FROM DIRECTOR_FILMS fd where FILM_ID = f.ID) as DIRECTOR_ID_LIST, " +
                "(SELECT GROUP_CONCAT(NAME) FROM directors d where ID in " +
                "(SELECT DIRECTOR_ID FROM DIRECTOR_FILMS fd where FILM_ID = f.ID)) as DIRECTORS_LIST, " +
                "FROM FILMS AS f " +
                "LEFT JOIN PUBLIC.MPA M on M.ID = f.MPA_ID " +
                "WHERE f.id IN (%s);";
        return jdbcTemplate.query(String.format(sql, inSql), new FilmMapper(), filmsId.toArray());
    }

    private Map<Integer, List<Integer>> getUsersLikes(SqlRowSet rowSet) {
        // Ключ -- id пользователя, значение -- множество id его фильмов.
        Map<Integer, List<Integer>> usersLikes = new HashMap<>();
        if (!rowSet.isBeforeFirst())
            return usersLikes;
        rowSet.next();
        do {
            int filmId = rowSet.getInt("FILM_ID");
            int userId = rowSet.getInt("USER_ID");
            if (!usersLikes.containsKey(userId))
                usersLikes.put(userId, new ArrayList<>());
            usersLikes.get(userId).add(filmId);
        } while (rowSet.next());
        return usersLikes;
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

    public void insertFilmDirector(Film film) {
        if (film.getDirectors() == null) {
            return;
        }
        List<Object[]> batchArgs = new ArrayList<>();
        for (Director director : film.getDirectors()) {
            batchArgs.add(new Object[]{film.getId(), director.getId()});
        }
        jdbcTemplate.batchUpdate("INSERT INTO DIRECTOR_FILMS(FILM_ID, DIRECTOR_ID) VALUES (?, ?)", batchArgs);
    }

    @Override
    public List<Film> loadFilmsOfDirectorSortedByYears(Long directorId) {
        String sqlQuery =
                "select f.*, " +
                        "     m.NAME as mpa_name, " +
                        "     (SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) " +
                        "     FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                        "     (SELECT GROUP_CONCAT(NAME) FROM genres g where ID in " +
                        "     (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS GENRES_LIST, " +
                        "     (SELECT GROUP_CONCAT(DIRECTOR_ID) " +
                        "     FROM DIRECTOR_FILMS fd where FILM_ID = f.ID) as DIRECTOR_ID_LIST, " +
                        "     (SELECT GROUP_CONCAT(NAME) FROM directors d where ID in " +
                        "     (SELECT DIRECTOR_ID FROM DIRECTOR_FILMS fd where FILM_ID = f.ID)) as DIRECTORS_LIST, " +
                        "     (SELECT GROUP_CONCAT(USER_ID) " +
                        "     FROM PUBLIC.FILMS_LIKES " +
                        "     WHERE FILM_ID = f.ID) as LIKES " +
                        "FROM films f " +
                        "LEFT JOIN mpa m on m.id = f.mpa_id " +
                        "LEFT JOIN PUBLIC.FILMS_LIKES FL on F.ID = FL.FILM_ID " +
                        "LEFT JOIN DIRECTOR_FILMS fd on fd.FILM_ID = f.ID " +
                        "LEFT JOIN directors d on fd.DIRECTOR_ID = d.ID " +
                        "where d.ID = ? " +
                        "group by f.ID, m.name  " +
                        "ORDER by extract(year from f.release_date), COUNT(FL.USER_ID) DESC, f.ID;";
        return jdbcTemplate.query(sqlQuery, new FilmMapper(), directorId);
    }

    @Override
    public List<Film> loadFilmsOfDirectorSortedByLikes(Long directorId) {
        String sqlQuery =
                "select f.*, " +
                        "     m.NAME as mpa_name, " +
                        "     (SELECT GROUP_CONCAT(GENRE_ID ORDER BY GENRE_ID) " +
                        "     FROM FILMS_GENRES WHERE FILM_ID = f.id) AS GENRES_ID_LIST, " +
                        "     (SELECT GROUP_CONCAT(NAME) FROM genres g where ID in " +
                        "     (SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = f.id)) AS GENRES_LIST, " +
                        "     (SELECT GROUP_CONCAT(DIRECTOR_ID) " +
                        "     FROM DIRECTOR_FILMS fd where FILM_ID = f.ID) as DIRECTOR_ID_LIST, " +
                        "     (SELECT GROUP_CONCAT(NAME) FROM directors d where ID in " +
                        "     (SELECT DIRECTOR_ID FROM DIRECTOR_FILMS fd where FILM_ID = f.ID)) as DIRECTORS_LIST, " +
                        "     (SELECT GROUP_CONCAT(USER_ID) " +
                        "     FROM PUBLIC.FILMS_LIKES " +
                        "     WHERE FILM_ID = f.ID) as LIKES " +
                        "FROM films f " +
                        "LEFT JOIN mpa m on m.id = f.mpa_id " +
                        "LEFT JOIN PUBLIC.FILMS_LIKES FL on F.ID = FL.FILM_ID " +
                        "LEFT JOIN DIRECTOR_FILMS fd on fd.FILM_ID = f.ID " +
                        "LEFT JOIN directors d on fd.DIRECTOR_ID = d.ID " +
                        "where d.ID = ? " +
                        "group by f.ID, m.name  " +
                        "ORDER by COUNT(FL.USER_ID) DESC, f.ID;";
        return jdbcTemplate.query(sqlQuery, new FilmMapper(), directorId);
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
                    .directors(getDirectors(rs))
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

        private Set<Director> getDirectors(ResultSet rs) throws SQLException {
            Set<Director> directors = new HashSet<>();

            String directorsIdList = rs.getString("DIRECTOR_ID_LIST");
            String directorsList = rs.getString("DIRECTORS_LIST");

            if (directorsIdList != null && directorsList != null) {
                String[] directorIds = directorsIdList.split(",");
                String[] directorsName = directorsList.split(",");

                for (int i = 0; i < directorIds.length; i++) {
                    directors.add(Director.builder()
                            .id(Long.parseLong(directorIds[i]))
                            .name(directorsName[i])
                            .build());
                }
            }
            return directors;
        }


        private Mpa getMpa(ResultSet rs) throws SQLException {
            return Mpa.builder()
                    .id(rs.getLong("MPA_ID"))
                    .name(rs.getString("MPA_NAME"))
                    .build();
        }
    }
}
