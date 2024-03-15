package ru.yandex.practicum.filmorate.exception;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class ValidateException extends RuntimeException {
    private String message;
}
