package ru.yandex.practicum.filmorate.repository.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserDbRepositoryImpl;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbRepositoryImplTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmRepository filmRepository;
    private UserRepository userRepository;
    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private Genre genre1 = Genre.builder()
            .id(1L)
            .name("Комедия")
            .build();
    private Genre genre2 = Genre.builder()
            .id(2L)
            .name("Драма")
            .build();
    private Set<Genre> genres = Set.of(genre1, genre2);
    private Set<Genre> genreOnlyId1 = Set.of(genre1);
    private Set<Genre> genreOnlyId2 = Set.of(genre2);


    @BeforeEach
    public void beforeEach() {
        userRepository = new UserDbRepositoryImpl(jdbcTemplate);
        filmRepository = new FilmDbRepositoryImpl(jdbcTemplate);
        film1 = Film.builder()
                .id(1L)
                .name("New Film")
                .description("Film description")
                .duration(100)
                .releaseDate(LocalDate.of(1987, 5, 15))
                .mpa(Mpa.builder()
                        .id(1L)
                        .name("G")
                        .build())
                .genres(genres)
                .build();

        film2 = Film.builder()
                .id(2L)
                .name("New Film2")
                .description("Film description2")
                .duration(70)
                .releaseDate(LocalDate.of(2000, 7, 3))
                .mpa(Mpa.builder()
                        .id(3L)
                        .name("PG-13")
                        .build())
                .genres(genreOnlyId2)
                .build();

        film3 = Film.builder()
                .id(3L)
                .name("New Film3")
                .description("Film description3")
                .duration(210)
                .releaseDate(LocalDate.of(2011, 10, 27))
                .mpa(Mpa.builder()
                        .id(2L)
                        .name("PG")
                        .build())
                .genres(genreOnlyId1)
                .build();
        user1 = User.builder()
                .id(1L)
                .email("user@email.ru")
                .login("vanya123")
                .name("Ivan Petrov")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    public void testSaveFilm() {
        Film savedFilm = filmRepository.saveFilm(film1);

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .ignoringFields("likes")
                .isEqualTo(film1);
    }

    @Test
    public void testFindUserById() {

        filmRepository.saveFilm(film1);

        // вызываем тестируемый метод
        Film savedFilm = filmRepository.getFilmById(1L);

        // проверяем утверждения
        assertThat(savedFilm)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .ignoringFields("likes")
                .isEqualTo(film1);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testGetAllUsers() {
        filmRepository.saveFilm(film1);
        filmRepository.saveFilm(film2);
        filmRepository.saveFilm(film3);

        List<Film> filmList = filmRepository.getAllFilms();

        assertThat(filmList.size())
                .isEqualTo(3);
    }

    @Test
    public void testUpdateFilm() {
        filmRepository.saveFilm(film1);
        film1.setName("Updated Film Name");
        Film updatedFilm = filmRepository.updateFilm(film1);

        assertThat(updatedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("likes")
                .isEqualTo(film1);
    }

    @Test
    public void testAddLike() {
        filmRepository.saveFilm(film1);
        userRepository.saveUser(user1);

        boolean isAdded = filmRepository.addLike(1L, 1L);
        assertTrue(isAdded);
        isAdded = filmRepository.addLike(1L, 1L);
        assertFalse(isAdded);
    }

    @Test
    public void testRemoveLike() {
        filmRepository.saveFilm(film1);
        userRepository.saveUser(user1);

        filmRepository.addLike(1L, 1L);
        boolean isRemoved = filmRepository.removeLike(1L, 1L);
        assertTrue(isRemoved);
        isRemoved = filmRepository.removeLike(1L, 1L);
        assertFalse(isRemoved);
    }

    @Test
    public void testExistsFilmById() {
        boolean isExists = filmRepository.existsFilmById(1L);
        assertFalse(isExists);

        filmRepository.saveFilm(film1);

        isExists = filmRepository.existsFilmById(1L);
        assertTrue(isExists);
    }
}