package ru.yandex.practicum.filmorate.repository.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Sql(scripts = "classpath:test_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreDbRepositoryImplTest {
    private final JdbcTemplate jdbcTemplate;
    private GenreRepository genreRepository;
    private Genre genre1;
    private Genre genre2;

    @BeforeEach
    public void beforeEach() {

        genreRepository = new GenreDbRepositoryImpl(jdbcTemplate);
        genre1 = Genre.builder()
                .id(1L)
                .name("Комедия")
                .build();
        genre2 = Genre.builder()
                .id(2L)
                .name("Драма")
                .build();
    }

    @Test
    public void testSaveGenre() {
        Genre savedGenre = genreRepository.saveGenre(genre1);
        assertThat(savedGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(genre1);
    }

    @Test
    public void testGetGenreById() {
        genreRepository.saveGenre(genre1);
        Genre genre = genreRepository.getGenreById(1L);
        assertThat(genre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(genre1);
    }

    @Test
    public void testGetAllGenre() {
        genreRepository.saveGenre(genre1);
        genreRepository.saveGenre(genre2);

        List<Genre> genreList = genreRepository.getAllGenres();
        assertThat(genreList.size())
                .isEqualTo(2);
    }

    @Test
    public void testUpdateGenre() {
        genreRepository.saveGenre(genre1);
        genre1.setName("Боевик");
        Genre updatedGenre = genreRepository.updateGenre(genre1);
        assertThat(updatedGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(genre1);
    }

    @Test
    public void testExistsGenreById() {
        boolean isExists = genreRepository.existsGenreById(1L);
        assertFalse(isExists);
        genreRepository.saveGenre(genre1);
        isExists = genreRepository.existsGenreById(1L);
        assertTrue(isExists);
    }
}