package ru.yandex.practicum.filmorate.repository.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
public class ReviewDbRepositoryImpl implements ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDbRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review saveReview(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID)" +
                            " VALUES (?, ?, ?, ?)",
                    new String[]{"REVIEW_ID"}
            );
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId().intValue());
            ps.setInt(4, review.getFilmId().intValue());
            return ps;
        }, keyHolder);

        Long reviewId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(reviewId);
        return getReviewById(reviewId);
    }

    @Override
    public Review getReviewById(Long id) {
        if (!existsReviewById(id)) {
            return null;
        }
        String sqlQuery = "SELECT r.*, " +
                "COUNT(rl.REVIEW_ID) - COUNT(rd.REVIEW_ID) AS useful " +
                "FROM REVIEWS AS r " +
                "LEFT JOIN REVIEWS_LIKES AS rl ON r.REVIEW_ID = rl.REVIEW_ID " +
                "LEFT JOIN REVIEWS_DISLIKES AS rd ON r.REVIEW_ID = rd.REVIEW_ID " +
                "WHERE r.REVIEW_ID = ?";

        return jdbcTemplate.queryForObject(sqlQuery, new ReviewMapper(), id);
    }

    @Override
    public List<Review> getAllReviews(Long filmId, int count) {
        String sql = "SELECT r.*, " +
                "COUNT(rl.REVIEW_ID) AS likes_count, COUNT(rd.REVIEW_ID) AS dislikes_count, " +
                "COUNT(rl.REVIEW_ID) - COUNT(rd.REVIEW_ID) AS useful " +
                "FROM REVIEWS AS r " +
                "LEFT JOIN REVIEWS_LIKES AS rl ON r.REVIEW_ID = rl.REVIEW_ID " +
                "LEFT JOIN REVIEWS_DISLIKES AS rd ON r.REVIEW_ID = rd.REVIEW_ID " +
                "WHERE r.FILM_ID = ? OR ? = 0 " +
                "GROUP BY r.REVIEW_ID, r.CONTENT, r.IS_POSITIVE, r.USER_ID, r.FILM_ID " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, new ReviewMapper(), filmId, filmId, count);
    }

    @Override
    public Review updateReview(Review review) {
        if (!existsReviewById(review.getReviewId())) {
            return null;
        }
        jdbcTemplate.update("UPDATE REVIEWS" +
                        " SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?",
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        return getReviewById(review.getReviewId());
    }

    @Override
    public boolean addLike(Long reviewId, Long userId) {
        String sqlQuery = "INSERT INTO REVIEWS_LIKES(REVIEW_ID, USER_ID) " +
                "SELECT ?, ? " +
                "WHERE NOT EXISTS(" +
                "    SELECT 1" +
                "    FROM REVIEWS_LIKES" +
                "    WHERE (REVIEW_ID = ? AND USER_ID = ?));";
        return jdbcTemplate.update(sqlQuery, reviewId, userId, reviewId, userId) > 0;
    }

    @Override
    public boolean removeLike(Long reviewId, Long userId) {
        String sqlQuery = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
        return jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;
    }

    @Override
    public boolean addDislike(Long reviewId, Long userId) {
        String sqlQuery = "INSERT INTO REVIEWS_DISLIKES(REVIEW_ID, USER_ID) " +
                "SELECT ?, ? " +
                "WHERE NOT EXISTS(" +
                "    SELECT 1" +
                "    FROM REVIEWS_LIKES" +
                "    WHERE (REVIEW_ID = ? AND USER_ID = ?));";
        return jdbcTemplate.update(sqlQuery, reviewId, userId, reviewId, userId) > 0;
    }

    @Override
    public boolean removeDislike(Long reviewId, Long userId) {
        String sqlQuery = "DELETE FROM REVIEWS_DISLIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
        return jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;
    }

    @Override
    public void deleteReview(Long id) {
        jdbcTemplate.update("DELETE FROM REVIEWS WHERE REVIEW_ID = ?", id);
    }

    @Override
    public boolean existsReviewById(Long reviewId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM REVIEWS WHERE REVIEW_ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reviewId.intValue()));
    }

    private static class ReviewMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder()
                    .reviewId(rs.getLong("REVIEW_ID"))
                    .content(rs.getString("CONTENT"))
                    .isPositive(rs.getBoolean("IS_POSITIVE"))
                    .userId(rs.getLong("USER_ID"))
                    .filmId(rs.getLong("FILM_ID"))
                    .useful(rs.getInt("USEFUL"))
                    .build();
        }
    }
}
