package ru.yandex.practicum.filmorate.repository.director;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
class DirectorDbRepositoryImplTest {

    Director director;
    final DirectorRepository directorRepository;


    @BeforeEach
    void setUp() {
        director = new Director(1L, "First Director");
    }

    @Test
    void findAllDirectorTest() {
        directorRepository.createDirector(director);
        Collection<Director> directorsListTest = directorRepository.getDirectors();
        assertEquals(1, directorsListTest.size());
    }

    @Test
    void getDirectorByIdTest() {
        directorRepository.createDirector(director);
        Director directorTest = directorRepository.getDirectorById(1L);
        assertEquals("First Director", directorTest.getName());
    }

    @Test
    void updateDirectorByIdTest() {
        directorRepository.createDirector(director);
        director.setName("Updated Director");
        directorRepository.updateDirector(director);
        Director directorTest = directorRepository.getDirectorById(1L);
        assertEquals("Updated Director", directorTest.getName());
    }

    @Test
    void deleteDirectorByIdTest() {
        directorRepository.createDirector(director);
        directorRepository.deleteDirectorById(director.getId());
        Collection<Director> directorsListTest = directorRepository.getDirectors();
        assertEquals(0, directorsListTest.size());
    }
}