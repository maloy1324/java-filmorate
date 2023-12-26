package ru.yandex.practicum.filmorate.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class NotFoundException extends RuntimeException {
    private String message;
    private HttpStatus status;
}
