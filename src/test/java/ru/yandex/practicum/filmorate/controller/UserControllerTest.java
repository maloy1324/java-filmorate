package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.util.ValidationUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {
    private UserController controller;

    User user = User.builder()
            .id(1)
            .email("user@gmail.com")
            .login("user")
            .name("user")
            .birthday(LocalDate.of(2003, 11, 5))
            .build();

    @BeforeEach
    void beforeEach() {
        Validator validator;
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
        UserRepository repository = new UserRepository();
        UserService service = new UserService(repository, new ValidationUtils(validator));
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
    void emptyName() {
        user.setName(null);
        controller.create(user);
        assertEquals("user", controller.findUser(1).getName());
    }
}