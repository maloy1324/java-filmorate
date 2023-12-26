package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {
    UserController controller;

    UserService service;

    UserRepository repository;

    User user = User.builder()
            .id(1)
            .email("user@gmail.com")
            .login("user")
            .name("user")
            .birthday(LocalDate.of(2003, 11, 5))
            .build();

    @BeforeEach
    void beforeEach() {
        repository = new UserRepository();
        service = new UserService(repository);
        controller = new UserController(service);
    }

    @Test
    void create() {
        controller.create(user);
        assertArrayEquals(List.of(user).toArray(), controller.findAll().toArray());
    }

    @Test
    void update() {
        controller.create(user);
        user.setName("newname1324");
        controller.update(user);
        assertArrayEquals(List.of(user).toArray(), controller.findAll().toArray());
    }

    @Test
    void invalidEmail() {
        user.setEmail("usergmail.com");
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(user);
        });
        Assertions.assertEquals("Электронная почта не может быть пустой и должна содержать символ '@'", exception.getMessage());
    }

    @Test
    void invalidLogin() {
        user.setLogin("some wrong login");
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(user);
        });
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void emptyName() {
        user.setName(null);
        controller.create(user);
        assertEquals("user", controller.findUser(1).getName());
    }

    @Test
    void invalidBirthday() {
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidateException exception = Assertions.assertThrows(ValidateException.class, () -> {
            controller.create(user);
        });
        assertEquals("Дата рождения не может быть в будущем.", exception.getMessage());
    }
}