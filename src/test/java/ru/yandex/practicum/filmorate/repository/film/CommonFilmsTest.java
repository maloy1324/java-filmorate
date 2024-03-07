package ru.yandex.practicum.filmorate.repository.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserDbRepositoryImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql(scripts = "/test_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql("/test_data.sql")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommonFilmsTest {
	private final JdbcTemplate jdbc;
	private FilmDbRepositoryImpl filmRepository;
	private UserDbRepositoryImpl userRepository;

	private Genre genre1 = Genre.builder()
			.id(1L)
			.name("Комедия")
			.build();
	private Genre genre2 = Genre.builder()
			.id(2L)
			.name("Драма")
			.build();
	private Set<Genre> genres = Set.of(genre1, genre2);
	private Film film1 = Film.builder()
			//.id(1L)
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
	private Film film2 = Film.builder()
			//.id(2L)
                .name("New Film2")
                .description("Film description2")
                .duration(70)
                .releaseDate(LocalDate.of(2000, 7, 3))
			.mpa(Mpa.builder()
                        .id(3L)
                        .name("PG-13")
                        .build())
			.genres(genres)
                .build();
	private Film film3 = Film.builder()
			//.id(3L)
                .name("New Film3")
                .description("Film description3")
                .duration(210)
                .releaseDate(LocalDate.of(2011, 10, 27))
			.mpa(Mpa.builder()
                        .id(2L)
                        .name("PG")
                        .build())
			.genres(genres)
                .build();
	private User user1 = User.builder()
			//.id(1L)
                .email("user@email.ru")
                .login("vanya123")
                .name("Ivan Petrov")
                .birthday(LocalDate.of(1990, 1, 1))
			.build();
	private User user2 = User.builder()
			//.id(2L)
			.email("user2@email.ru")
			.login("petya123")
			.name("Peter Ivanov")
			.birthday(LocalDate.of(1992, 2, 2))
			.build();
	private User user3 = User.builder()
			//.id(3L)
			.email("user3@email.ru")
			.login("fedor123")
			.name("Fedor Fedorov")
			.birthday(LocalDate.of(1993, 3, 3))
			.build();

	@BeforeEach
	void createRepositories() {
		filmRepository = new FilmDbRepositoryImpl(jdbc);
		userRepository = new UserDbRepositoryImpl(jdbc);
	}

	@Test
	void find_common_films_test_usersId_1_2() {
		fillRepositoryWithData();
		List<Film> commonFilms = filmRepository.findCommonFilms(1L, 2L);
		List<Film> expected = List.of(film1, film2, film3);
		assertFilmsListsEquals(expected, commonFilms);
	}

	@Test
	void find_common_films_test_usersId_2_1() {
		fillRepositoryWithData();
		List<Film> commonFilms = filmRepository.findCommonFilms(2L, 1L);
		List<Film> expected = List.of(film1, film2, film3);
		assertFilmsListsEquals(expected, commonFilms);
	}

	@Test
	void find_common_films_test_usersId_1_3() {
		fillRepositoryWithData();
		List<Film> commonFilms = filmRepository.findCommonFilms(1L, 3L);
		List<Film> expected = List.of(film1, film3);
		assertFilmsListsEquals(expected, commonFilms);
	}

	@Test
	void find_common_films_test_usersId_3_1() {
		fillRepositoryWithData();
		List<Film> commonFilms = filmRepository.findCommonFilms(3L, 1L);
		List<Film> expected = List.of(film1, film3);
		assertFilmsListsEquals(expected, commonFilms);
	}

	@Test
	void find_common_films_test_usersId_2_3() {
		fillRepositoryWithData();
		List<Film> commonFilms = filmRepository.findCommonFilms(2L, 3L);
		List<Film> expected = List.of(film1, film3);
		assertFilmsListsEquals(expected, commonFilms);
	}

	@Test
	void find_common_films_test_usersId_3_2() {
		fillRepositoryWithData();
		List<Film> commonFilms = filmRepository.findCommonFilms(3L, 2L);
		List<Film> expected = List.of(film1, film3);
		assertFilmsListsEquals(expected, commonFilms);
	}

	private void assertFilmsListsEquals(List<Film> expectedList, List<Film> actualList) {
		assertEquals(expectedList.size(), actualList.size());
		for (int i = 0; i < expectedList.size(); i++) {
			Film expectedFilm = expectedList.get(i);
			Film actualFilm = actualList.get(i);
			assertEquals(expectedFilm.getId(), actualFilm.getId());
			assertEquals(expectedFilm.getName(), actualFilm.getName());
		}
	}

	private void fillRepositoryWithData() {
		filmRepository.saveFilm(film1);
		filmRepository.saveFilm(film2);
		filmRepository.saveFilm(film3);

		userRepository.saveUser(user1);
		userRepository.saveUser(user2);
		userRepository.saveUser(user3);

		filmRepository.addLike(1L, 1L);
		filmRepository.addLike(1L, 2L);
		filmRepository.addLike(1L, 3L);

		filmRepository.addLike(2L, 1L);
		filmRepository.addLike(2L, 2L);

		filmRepository.addLike(3L, 1L);
		filmRepository.addLike(3L, 2L);
		filmRepository.addLike(3L, 3L);
	}


}