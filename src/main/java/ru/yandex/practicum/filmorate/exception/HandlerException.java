package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.model.ResponseError;

@Slf4j
@RestControllerAdvice
public class HandlerException {
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<ResponseError> validate(ValidateException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(ResponseError.builder()
                .message("Ошибка: " + e.getMessage())
                .build(), e.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseError> notFound(NotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(ResponseError.builder()
                .message("Ошибка: " + e.getMessage())
                .build(), e.getStatus());
    }
}
