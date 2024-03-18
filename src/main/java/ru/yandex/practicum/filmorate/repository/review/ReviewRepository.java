package ru.yandex.practicum.filmorate.repository.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewRepository {
    Review saveReview(Review review);

    Review getReviewById(Long id);

    List<Review> getAllReviews(Long filmId, int count);

    Review updateReview(Review review);

    boolean addLike(Long reviewId, Long userId);

    boolean removeLike(Long reviewId, Long userId);

    boolean addDislike(Long reviewId, Long userId);

    boolean removeDislike(Long reviewId, Long userId);

    void deleteReview(Long id);

    boolean existsReviewById(Long reviewId);
}
