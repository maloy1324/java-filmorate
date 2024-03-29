package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody @Valid User user) {
        User newUser = userService.addUser(user);
        log.info("Пользователь {} добавлен.", user.getName());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        User updatedUser = userService.updateUser(user);
        log.info("Пользователь {} обновлён.", user.getName());
        return updatedUser;
    }

    @GetMapping
    public Collection<User> findAllUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{accountId}")
    public User findUser(@PathVariable Long accountId) {
        return userService.getUser(accountId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriendById(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFriendById(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
