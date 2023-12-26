package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private int globalId = 0;

    public User getUserForId(Integer accountId) {
        return users.get(accountId);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public void save(Integer id, User user) {
        users.put(id, user);
    }

    public int generateId() {
        return ++globalId;
    }
}
