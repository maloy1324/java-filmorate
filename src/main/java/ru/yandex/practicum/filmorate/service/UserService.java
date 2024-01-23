package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserRepositoryImpl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepositoryImpl repository;

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
        return repository.save(validateName(user));
    }

    public void addFriend(Long userId, Long friendId) {
        checkId(userId, friendId);
        if (repository.getUserById(userId).getFriendsId().contains(friendId)) {
            throw new ValidateException("Пользователь уже добавлен у друзья", BAD_REQUEST);
        }
        repository.getUserById(userId).getFriendsId().add(friendId);
        repository.getUserById(friendId).getFriendsId().add(userId);
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
        repository.getUserById(userId).getFriendsId().remove(friendId);
        log.info("(ID: {}) удалён из друзей (ID: {})", friendId, userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        checkId(userId, otherId);
        log.info("Список общих друзей отправлен");
        Set<Long> userFriends = repository.getUserById(userId).getFriendsId();
        return repository.getUserById(otherId).getFriendsId().stream()
                .filter(userFriends::contains)
                .map(repository::getUserById)
                .collect(Collectors.toList());
    }

    public User updateUser(User user) {
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

    private void checkId(Long id, Long otherId) {
        if (!repository.existsUserById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден", NOT_FOUND);
        }
        if (!repository.existsUserById(otherId)) {
            throw new NotFoundException("Пользователь с id " + otherId + " не найден", NOT_FOUND);
        }
    }
}
