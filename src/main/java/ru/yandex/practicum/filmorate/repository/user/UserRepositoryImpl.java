package ru.yandex.practicum.filmorate.repository.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long globalId = 0L;

    @Override
    public User saveUser(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(Long accountId) {
        return users.get(accountId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        }
        return null;
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        if (getUserById(userId).getFriendsId().contains(friendId)) {
            throw new ValidateException("Пользователь уже добавлен у друзья");
        }
        getUserById(userId).getFriendsId().add(friendId);
        getUserById(friendId).getFriendsId().add(userId);
        return true;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        getUserById(id).getFriendsId().remove(friendId);
        getUserById(friendId).getFriendsId().remove(id);
    }

    @Override
    public List<User> findAllFriends(Long userId) {
        return users.get(userId).getFriendsId().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long anotherUserId) {
        Set<Long> userFriends = getUserById(userId).getFriendsId();
        Set<Long> anotherUserFriends = getUserById(anotherUserId).getFriendsId();
        if (userFriends != null && anotherUserFriends != null) {
            return anotherUserFriends.stream()
                    .filter(userFriends::contains)
                    .map(this::getUserById)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean existsUserById(Long userId) {
        return users.containsKey(userId);
    }

    private Long generateId() {
        return ++globalId;
    }
}
