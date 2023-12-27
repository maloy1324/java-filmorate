package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.util.ValidationUtils;

import java.util.Collection;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final ValidationUtils validator;

    public User getUser(Integer accountId) {
        User user = repository.getUserById(accountId);
        if (user == null) {
            throw new NotFoundException("Пользователь с указанным id не найден", NOT_FOUND);
        }
        return user;
    }

    public Collection<User> getUsers() {
        return repository.getAllUsers();
    }

    public User addUser(User user) {
        validator.validationRequest(user);
        return repository.save(validateName(user));
    }

    public User updateUser(User user) {
        validator.validationRequest(user);
        User updatedUser = repository.update(validateName(user));
        if (updatedUser == null) {
            throw new NotFoundException("Пользователь с указанным id не найден", NOT_FOUND);
        }
        return updatedUser;
    }

    private User validateName(User user) {
        String userName = user.getName();
        if (userName == null || userName.isEmpty()) {
            user.setName(user.getLogin());
            log.info("Для имени использовано значение логина");
        }
        return user;
    }
}
