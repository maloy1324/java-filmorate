package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {
    private Validator validator;
    private User user = User.builder()
            .id(1L)
            .email("user@gmail.com")
            .login("user")
            .name("username")
            .birthday(LocalDate.of(2003, 11, 5))
            .build();

    @BeforeEach
    void setUp() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @Test
    void invalidEmail() {
        user.setEmail("gmail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals("Email не корректен", violations.iterator().next().getMessage());
    }

    @Test
    void invalidLoginEmpty() {
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void invalidLoginSpace() {
        user.setLogin("user name");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void invalidBirthday() {
        user.setBirthday(LocalDate.of(2025, 1, 1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }
}