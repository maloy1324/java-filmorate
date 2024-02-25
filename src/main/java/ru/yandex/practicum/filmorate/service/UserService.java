package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.Collection;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(@Qualifier("UserDbRepositoryImpl") UserRepository repository) {
        this.repository = repository;
    }

    public User getUser(Long accountId) {
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
        return repository.saveUser(validateName(user));
    }

    public void addFriend(Long userId, Long friendId) {
        checkId(userId, friendId);
        boolean isAdded = repository.addFriend(userId, friendId);
        if (!isAdded) {
            throw new BadRequestException("Пользователь уже добавлен в друзья", BAD_REQUEST);
        }
        log.info("(ID: {}) добавлен в друзья к (ID: {})", friendId, userId);
    }

    public List<User> getAllFriends(Long userId) {
        if (!repository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        log.info("Список друзей отправлен.");
        return repository.findAllFriends(userId);
    }

    public void deleteFriendById(Long userId, Long friendId) {
        repository.deleteFriend(userId, friendId);
        log.info("(ID: {}) удалён из друзей (ID: {})", friendId, userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        checkId(userId, otherId);
        log.info("Список общих друзей отправлен");
        return repository.getCommonFriends(userId, otherId);
    }

    public User updateUser(User user) {
        User updatedUser = repository.updateUser(validateName(user));
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

    private void checkId(Long id, Long otherId) {
        if (!repository.existsUserById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден", NOT_FOUND);
        }
        if (!repository.existsUserById(otherId)) {
            throw new NotFoundException("Пользователь с id " + otherId + " не найден", NOT_FOUND);
        }
    }
}
