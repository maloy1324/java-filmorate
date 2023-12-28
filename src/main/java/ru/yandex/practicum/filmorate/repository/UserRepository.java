package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private int globalId = 0;

    public User getUserById(Integer accountId) {
        return users.get(accountId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User save(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        }
        return null;
    }

    private int generateId() {
        return ++globalId;
    }
}
