package ru.yandex.practicum.filmorate.repository.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository(value = "userDbRepositoryImpl")
public class UserDbRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserDbRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User saveUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)",
                    new String[]{"ID"}
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return getUserById(userId);

    }

    @Override
    public User getUserById(Long accountId) {
        if (!existsUserById(accountId)) {
            return null;
        }
        String sqlQuery = "SELECT u.*, GROUP_CONCAT(f.friend_id) AS friends_id " +
                "FROM USERS AS u" +
                "         LEFT JOIN (" +
                "    SELECT USER1_ID AS user_id, USER2_ID AS friend_id" +
                "    FROM FRIENDSHIPS" +
                "    UNION ALL" +
                "    SELECT USER2_ID AS user_id, USER1_ID AS friend_id" +
                "    FROM FRIENDSHIPS" +
                "    WHERE STATUS = true" +
                ") AS f ON u.ID = f.user_id " +
                "WHERE u.ID = ?;";
        return jdbcTemplate.queryForObject(sqlQuery, new UserMapper(), accountId);
    }

    @Override
    public List<User> getAllUsers() {
        String sqlQuery = "SELECT u.*, GROUP_CONCAT(f.friend_id) AS friends_id " +
                "FROM USERS AS u" +
                "         LEFT JOIN (" +
                "    SELECT USER1_ID AS user_id, USER2_ID AS friend_id" +
                "    FROM FRIENDSHIPS" +
                "    UNION ALL" +
                "    SELECT USER2_ID AS user_id, USER1_ID AS friend_id" +
                "    FROM FRIENDSHIPS" +
                "    WHERE STATUS = true" +
                ") AS f ON u.ID = f.user_id " +
                "GROUP BY u.ID;";
        return jdbcTemplate.query(sqlQuery, new UserMapper());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long anotherUserId) {
        String sqlQuery = "SELECT u.*," +
                "       (SELECT GROUP_CONCAT(" +
                "                       CASE" +
                "                           WHEN F.USER1_ID = u.ID THEN F.USER2_ID" +
                "                           WHEN F.USER2_ID = u.ID AND F.STATUS = true THEN F.USER1_ID END SEPARATOR ','" +
                "               )" +
                "        FROM FRIENDSHIPS as F) as friends_id " +
                "FROM USERS as u " +
                "WHERE ID IN (" +
                "    SELECT f.friend_id" +
                "    FROM (SELECT USER1_ID AS user_id, USER2_ID AS friend_id" +
                "          FROM FRIENDSHIPS" +
                "          UNION ALL" +
                "          SELECT USER2_ID AS user_id, USER1_ID AS friend_id" +
                "          FROM FRIENDSHIPS" +
                "          WHERE STATUS = true) AS f" +
                "    WHERE f.user_id = ?" +
                "    INTERSECT" +
                "    SELECT f.friend_id" +
                "    FROM (SELECT USER1_ID AS user_id, USER2_ID AS friend_id" +
                "          FROM FRIENDSHIPS" +
                "          UNION ALL" +
                "          SELECT USER2_ID AS user_id, USER1_ID AS friend_id" +
                "          FROM FRIENDSHIPS" +
                "          WHERE STATUS = true) AS f" +
                "    WHERE f.user_id = ?);";
        return jdbcTemplate.query(sqlQuery, new UserMapper(), userId, anotherUserId);
    }

    @Override
    public User updateUser(User user) {
        if (!existsUserById(user.getId())) {
            return null;
        }
        jdbcTemplate.update("UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE ID = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO FRIENDSHIPS (USER1_ID, USER2_ID)" +
                " SELECT ?, ?" +
                " FROM dual" +
                " WHERE NOT EXISTS(" +
                "SELECT 1" +
                " FROM FRIENDSHIPS" +
                " WHERE (USER1_ID = ? AND USER2_ID = ?)" +
                " OR (USER1_ID = ? AND USER2_ID = ?))";

        return jdbcTemplate.update(sql, userId, friendId, userId, friendId, friendId, userId) > 0;
    }

    @Override
    public boolean deleteUser(Long id) {
        jdbcTemplate.update("DELETE FROM USERS WHERE ID = ?", id);
        return existsUserById(id);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        String sqlQuery = "DELETE FROM friendships " +
                "WHERE (USER1_ID = ? AND USER2_ID = ?);";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public List<User> findAllFriends(Long userId) {
        String sql = "SELECT u.*," +
                "       (SELECT GROUP_CONCAT(" +
                "                       CASE" +
                "                           WHEN F.USER1_ID = u.ID THEN F.USER2_ID" +
                "                           WHEN F.USER2_ID = u.ID AND F.STATUS = true THEN F.USER1_ID END SEPARATOR ','" +
                "               )" +
                "        FROM FRIENDSHIPS as F) as friends_id " +
                "FROM USERS as u " +
                "WHERE u.ID in (SELECT (" +
                "                          CASE" +
                "                              WHEN F.USER1_ID = ? THEN F.USER2_ID" +
                "                              WHEN F.USER2_ID = ? AND F.STATUS = true THEN F.USER1_ID END" +
                "                          )" +
                "               FROM FRIENDSHIPS as F);";
        return jdbcTemplate.query(sql, new UserMapper(), userId, userId);
    }

    @Override
    public boolean existsUserById(Long userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM USERS WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId.intValue()));
    }

    private static class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long userId = rs.getLong("ID");

            return User.builder()
                    .id(userId)
                    .email(rs.getString("EMAIL"))
                    .login(rs.getString("LOGIN"))
                    .name(rs.getString("NAME"))
                    .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                    .friendsId(getFriendsId(rs))
                    .build();
        }

        public Set<Long> getFriendsId(ResultSet rs) throws SQLException {
            Set<Long> friendsId = new HashSet<>();
            String stringIds = rs.getString("friends_id");
            if (stringIds != null) {
                String[] splitIds = stringIds.split(",");
                for (String splitId : splitIds) {
                    friendsId.add(Long.parseLong(splitId));
                }
            }
            return friendsId;
        }
    }
}