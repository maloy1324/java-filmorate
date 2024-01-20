package ru.yandex.practicum.filmorate.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = AfterDateValidator.class)

public @interface AfterDate {
    String message() default "Дата не может быть раньше {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value() default "1970-01-01";
}