package ru.yandex.practicum.filmorate.repository.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {
    User getUserById(Long accountId);

    List<User> getAllUsers();

    User save(User user);

    User update(User user);

    List<User> findAllFriends(Long userId);

    boolean existsUserById(Long userId);
}
