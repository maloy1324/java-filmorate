package ru.yandex.practicum.filmorate.repository.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {
    User getUserById(Long accountId);

    List<User> getAllUsers();

    User save(User user);

    User update(User user);

    void addFriend(Long userId, Long friendId);

    List<User> findAllFriends(Long userId);

    void deleteFriendById(Long userId, Long friendId);

    List<User> findCommonFriends(Long userId, Long otherId);

    boolean existsUserById(Long userId);
}
