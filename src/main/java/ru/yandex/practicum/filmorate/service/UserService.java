package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collection;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User getUser(Integer accountId) {
        User user = repository.getUserForId(accountId);
        if (user == null) {
            throw new NotFoundException("Пользователь с указанным id не найден", NOT_FOUND);
        }
        return user;
    }

    public Collection<User> getUsers() {
        return repository.getAllUsers();
    }

    public User addUser(User user) {
        User validatedUser = validate(user);
        if (repository.getUserForId(validatedUser.getId()) == null) {
            user.setId(repository.generateId());
        }
        repository.save(user.getId(), user);
        log.info("Пользователь добавлен.");
        return repository.getUserForId(user.getId());
    }

    public User updateUser(User user) {
        if (repository.getUserForId(user.getId()) == null) {
            throw new NotFoundException("Пользователь с указанным id не найден", NOT_FOUND);
        }
        repository.save(user.getId(), validate(user));
        log.info("Пользователь обновлён.");
        return repository.getUserForId(user.getId());
    }

    private User validate(User user) {
        String userEmail = user.getEmail();
        String userLogin = user.getLogin();
        String userName = user.getName();
        LocalDate userBirthday = user.getBirthday();
        if (userEmail == null || !userEmail.contains("@")) {
            throw new ValidateException("Электронная почта не может быть пустой и должна содержать символ '@'", BAD_REQUEST);
        }
        if (userLogin == null || userLogin.isEmpty() || userLogin.isBlank() || userLogin.contains(" ")) {
            throw new ValidateException("Логин не может быть пустым и содержать пробелы", BAD_REQUEST);
        }
        if (userBirthday.isAfter(LocalDate.now())) {
            throw new ValidateException("Дата рождения не может быть в будущем.", BAD_REQUEST);
        }
        if (userName == null || userName.isEmpty() || userName.isBlank()) {
            user.setName(userLogin);
        }
        return user;
    }
}
