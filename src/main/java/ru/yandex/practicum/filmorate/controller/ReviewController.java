package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

import static ru.yandex.practicum.filmorate.constant.Constants.DEFAULT_COUNT;
import static ru.yandex.practicum.filmorate.constant.Constants.DEFAULT_ID_VALUE;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review createReview(@RequestBody @Valid Review review) {
        Review newReview = reviewService.addReview(review);
        log.info("Отзыв добавлен");
        return newReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        Review updatedReview = reviewService.updateReview(review);
        log.info("Отзыв обновлен");
        return updatedReview;
    }

    @GetMapping
    public Collection<Review> findAllReviews(@RequestParam(defaultValue = DEFAULT_ID_VALUE) String filmId,
                                             @RequestParam(defaultValue = DEFAULT_COUNT) String count) {
        return reviewService.findAll(filmId, count);
    }

    @GetMapping("/{reviewId}")
    public Review findReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        log.info("Отзыв c ID {} удалён.", reviewId);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeLike(id, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeDislike(id, userId);
    }
}
