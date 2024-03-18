package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Review {
    private Long reviewId;
    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;
    @NotNull(message = "Требуется указать, является ли отзыв положительным, или негативным")
    private Boolean isPositive;
    @NotNull(message = "Не указан пользователь, которому принадлежит отзыв")
    private Long userId;
    @NotNull(message = "Не указан фильм, которому принадлежит отзыв")
    private Long filmId;
    private Integer useful;

}
