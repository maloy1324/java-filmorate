package ru.yandex.practicum.filmorate.repository.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Sql(scripts = "classpath:test_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaDbRepositoryImplTest {
    private final JdbcTemplate jdbcTemplate;
    private MpaRepository mpaRepository;
    private Mpa mpa1;
    private Mpa mpa2;

    @BeforeEach
    public void beforeEach() {

        mpaRepository = new MpaDbRepositoryImpl(jdbcTemplate);
        mpa1 = Mpa.builder()
                .id(1L)
                .name("G")
                .build();
        mpa2 = Mpa.builder()
                .id(2L)
                .name("PG")
                .build();
    }

    @Test
    public void testSaveMpa() {
        Mpa savedMpa = mpaRepository.saveMpa(mpa1);
        assertThat(savedMpa)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(mpa1);
    }

    @Test
    public void testGetMpaById() {
        mpaRepository.saveMpa(mpa1);
        Mpa mpa = mpaRepository.getMpaById(1L);
        assertThat(mpa)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(mpa1);
    }

    @Test
    public void testGetAllMpa() {
        mpaRepository.saveMpa(mpa1);
        mpaRepository.saveMpa(mpa2);

        List<Mpa> mpaList = mpaRepository.getAllMpa();
        assertThat(mpaList.size())
                .isEqualTo(2);
    }

    @Test
    public void testUpdateMpa() {
        mpaRepository.saveMpa(mpa1);
        mpa1.setName("Боевик");
        Mpa updatedMpa = mpaRepository.updateMpa(mpa1);
        assertThat(updatedMpa)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(mpa1);
    }

    @Test
    public void testExistsMpaById() {
        boolean isExists = mpaRepository.existsMpaById(1L);
        assertFalse(isExists);
        mpaRepository.saveMpa(mpa1);
        isExists = mpaRepository.existsMpaById(1L);
        assertTrue(isExists);
    }
}