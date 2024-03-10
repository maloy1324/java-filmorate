package ru.yandex.practicum.filmorate.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.constant.Constants.DATE_FORMATTER;

public class AfterDateValidator implements ConstraintValidator<AfterDate, LocalDate> {
    private LocalDate beforeDate;

    @Override
    public void initialize(AfterDate constraintAnnotation) {
        beforeDate = LocalDate.parse(constraintAnnotation.value(), DATE_FORMATTER);
    }

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
        return !releaseDate.isBefore(beforeDate);
    }
}