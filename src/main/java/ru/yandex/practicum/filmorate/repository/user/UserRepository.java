package ru.yandex.practicum.filmorate.repository.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {
    User saveUser(User user);

    User getUserById(Long accountId);

    List<User> getAllUsers();

    List<User> getCommonFriends(Long userId, Long anotherUserId);

    User updateUser(User user);

    boolean addFriend(Long userId, Long friendId);

    void deleteUser(Long id);

    void deleteFriend(Long id, Long friendId);

    List<User> findAllFriends(Long userId);

    boolean existsUserById(Long userId);
}
