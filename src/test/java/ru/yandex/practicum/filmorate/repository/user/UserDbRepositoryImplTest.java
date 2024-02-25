package ru.yandex.practicum.filmorate.repository.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbRepositoryImplTest {
    private final JdbcTemplate jdbcTemplate;
    private UserRepository userRepository;
    private User user1;
    private User user2;
    private User user3;


    @BeforeEach
    public void beforeEach() {
        userRepository = new UserDbRepositoryImpl(jdbcTemplate);
        user1 = User.builder()
                .id(1L)
                .email("user@email.ru")
                .login("vanya123")
                .name("Ivan Petrov")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user2 = User.builder()
                .id(2L)
                .email("user2@gmail.com")
                .login("igor1244")
                .name("Igor Ivanov")
                .birthday(LocalDate.of(2000, 5, 17))
                .build();
        user3 = User.builder()
                .id(3L)
                .email("user3@ya.ru")
                .login("anton34")
                .name("Anton Logvinov")
                .birthday(LocalDate.of(1984, 10, 23))
                .build();
    }

    @Test
    public void testSaveUser() {
        User savedUser = userRepository.saveUser(user1);

        assertThat(savedUser)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .ignoringFields("friendsId")
                .isEqualTo(user1);
    }

    @Test
    public void testFindUserById() {

        userRepository.saveUser(user1);

        // вызываем тестируемый метод
        User savedUser = userRepository.getUserById(1L);

        // проверяем утверждения
        assertThat(savedUser)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .ignoringFields("friendsId")
                .isEqualTo(user1);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testGetAllUsers() {
        userRepository.saveUser(user1);
        userRepository.saveUser(user2);
        userRepository.saveUser(user3);

        List<User> usersList = userRepository.getAllUsers();

        assertThat(usersList.size())
                .isEqualTo(3);
    }

    @Test
    public void testAddFriend() {
        userRepository.saveUser(user1);
        userRepository.saveUser(user2);

        boolean isAdded = userRepository.addFriend(1L, 2L);
        assertTrue(isAdded);
        isAdded = userRepository.addFriend(1L, 2L);
        assertFalse(isAdded);
    }

    @Test
    public void testGetAllFriends() {
        userRepository.saveUser(user1);
        userRepository.saveUser(user2);
        userRepository.addFriend(1L, 2L);

        List<User> friends = userRepository.findAllFriends(1L);
        assertThat(friends.size())
                .isEqualTo(1);
    }

    @Test
    public void testGetCommonFriends() {
        userRepository.saveUser(user1);
        userRepository.saveUser(user2);
        userRepository.saveUser(user3);

        userRepository.addFriend(1L, 2L);
        userRepository.addFriend(1L, 3L);
        userRepository.addFriend(2L, 3L);

        List<User> commonFriends = userRepository.getCommonFriends(1L, 2L);
        assertThat(commonFriends.size())
                .isEqualTo(1);
    }

    @Test
    public void testUpdateUser() {
        userRepository.saveUser(user1);
        user1.setName("New Name");
        User updatedUser = userRepository.updateUser(user1);
        assertThat(updatedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("friendsId")
                .isEqualTo(user1);
    }

    @Test
    public void testDeleteFriend() {
        userRepository.saveUser(user1);
        userRepository.saveUser(user2);

        userRepository.addFriend(1L, 2L);
        userRepository.deleteFriend(1L, 2L);

        List<User> friends = userRepository.findAllFriends(1L);
        assertThat(friends.size())
                .isEqualTo(0);
    }

    @Test
    public void testExistsUserById() {
        boolean isExists = userRepository.existsUserById(1L);
        assertFalse(isExists);

        userRepository.saveUser(user1);

        isExists = userRepository.existsUserById(1L);
        assertTrue(isExists);
    }
}