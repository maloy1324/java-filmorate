package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Review {
    Long reviewId;
    @NotBlank(message = "Содержание отзыва не может быть пустым")
    String content;
    @NotNull(message = "Требуется указать, является ли отзыв положительным, или негативным")
    Boolean isPositive;
    @NotNull(message = "Не указан пользователь, которому принадлежит отзыв")
    Long userId;
    @NotNull(message = "Не указан фильм, которому принадлежит отзыв")
    Long filmId;
    Integer useful;
}
