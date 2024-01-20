package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class HandlerException {
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<ResponseError> validate(ValidateException e) {
        return new ResponseEntity<>(ResponseError.builder()
                .message("Ошибка вадидации: " + e.getMessage())
                .build(), e.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseError> notFound(NotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(ResponseError.builder()
                .message(e.getMessage())
                .build(), e.getStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ResponseError> numberFormat(NumberFormatException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(ResponseError.builder()
                .message(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseError> throwableException(Exception e) {
        log.error("Возникла непредвиденная ошибка", e);
        return new ResponseEntity<>(ResponseError.builder()
                .message(e.getMessage())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}