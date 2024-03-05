package ru.yandex.practicum.filmorate.repository.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class MpaDbRepositoryImpl implements MpaRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Mpa saveMpa(Mpa mpa) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO MPA (NAME)" +
                    " VALUES (?)", new String[]{"ID"});
            ps.setString(1, mpa.getName());
            return ps;
        }, keyHolder);

        Long mpaId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        mpa.setId(mpaId);

        return mpa;
    }

    @Override
    public Mpa getMpaById(Long id) {
        if (!existsMpaById(id)) {
            return null;
        }
        String sqlQuery = "SELECT * FROM MPA WHERE ID = ?";
        return jdbcTemplate.queryForObject(sqlQuery, new MpaMapper(), id);
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sqlQuery = "SELECT * FROM MPA ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, new MpaMapper());
    }

    @Override
    public Mpa updateMpa(Mpa mpa) {
        if (!existsMpaById(mpa.getId())) {
            return null;
        }
        String sqlQuery = "UPDATE MPA " +
                "SET NAME = ? WHERE ID = ?";
        jdbcTemplate.update(sqlQuery, mpa.getName(), mpa.getId());
        return mpa;
    }

    @Override
    public boolean existsMpaById(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM MPA WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id.intValue()));
    }

    private static class MpaMapper implements RowMapper<Mpa> {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Mpa.builder()
                    .id(rs.getLong("ID"))
                    .name(rs.getString("NAME"))
                    .build();
        }
    }
}
