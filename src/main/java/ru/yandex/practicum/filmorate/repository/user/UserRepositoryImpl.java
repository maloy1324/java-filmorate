package ru.yandex.practicum.filmorate.repository.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long globalId = 0L;

    @Override
    public User save(User user) {
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
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        }
        return null;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        users.get(userId).getFriendsId().add(friendId);
        users.get(friendId).getFriendsId().add(userId);
    }

    @Override
    public List<User> findAllFriends(Long userId) {
        return users.get(userId).getFriendsId().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFriendById(Long userId, Long friendId) {
        users.get(userId).getFriendsId().remove(friendId);
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = users.get(userId).getFriendsId();
        return users.get(otherId).getFriendsId().stream()
                .filter(userFriends::contains)
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsUserById(Long userId) {
        return users.containsKey(userId);
    }

    private Long generateId() {
        return ++globalId;
    }
}
