package ru.yandex.practicum.filmorate.exception;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class NotFoundException extends RuntimeException {
    private String message;
}
