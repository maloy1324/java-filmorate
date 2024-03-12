package ru.yandex.practicum.filmorate.repository.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
@Repository
@RequiredArgsConstructor
public class FeedDbRepositoryImpl implements FeedRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveFeed(Feed feed) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO FEED (USER_ID, ENTITY_ID, EVENT_TYPE, OPERATION, TIMESTAMP)" +
                            " VALUES (?, ?, ?, ?, ?)",
                    new String[]{"EVENT_ID"}
            );
            ps.setInt(1, feed.getUserId().intValue());
            ps.setInt(2, feed.getEntityId().intValue());
            ps.setString(3, feed.getEventType());
            ps.setString(4, feed.getOperation());
            ps.setLong(5, feed.getTimestamp());
            return ps;
        }, keyHolder);
        Long feedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        feed.setEventId(feedId);
    }

    @Override
    public List<Feed> getFeedForUser(Long userId) {
        return jdbcTemplate.query("SELECT * FROM FEED WHERE USER_ID = ?",
                new BeanPropertyRowMapper<>(Feed.class), userId.intValue());
    }
}
