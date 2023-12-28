package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody User user) {
        User newUser = userService.addUser(user);
        log.info("Пользователь {} добавлен.", user.getName());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        log.info("Пользователь {} обновлён.", user.getName());
        return updatedUser;
    }

    @GetMapping
    public Collection<User> findAllUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{accountId}")
    public User findUser(@PathVariable Integer accountId) {
        return userService.getUser(accountId);
    }
}
