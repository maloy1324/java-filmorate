package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.constant.EventTypes;
import ru.yandex.practicum.filmorate.constant.Operations;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.feed.FeedRepository;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.review.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.Collection;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    private final FeedRepository feedRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         @Qualifier("userDbRepositoryImpl") UserRepository userRepository,
                         @Qualifier("filmDbRepositoryImpl") FilmRepository filmRepository,
                         FeedRepository feedRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.feedRepository = feedRepository;
    }

    public Review addReview(Review review) {
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();
        boolean userExists = userRepository.existsUserById(userId);
        boolean filmExists = filmRepository.existsFilmById(filmId);
        if (!userExists) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        if (!filmExists) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден", NOT_FOUND);
        }
        Review savedReview = reviewRepository.saveReview(review);
        feedRepository.saveFeed(new Feed(null, userId, review.getReviewId(), EventTypes.REVIEW.toString(),
                Operations.ADD.toString(), System.currentTimeMillis()));
        return savedReview;
    }

    public Review updateReview(Review review) {
        Review updatedReview = reviewRepository.updateReview(review);
        if (updatedReview == null) {
            throw new NotFoundException("Отзыв не найден", NOT_FOUND);
        }
        feedRepository.saveFeed(new Feed(null, updatedReview.getUserId(), updatedReview.getReviewId(), EventTypes.REVIEW.toString(),
                Operations.UPDATE.toString(), System.currentTimeMillis()));
        return updatedReview;
    }

    public Review getReview(Long id) {
        Review review = reviewRepository.getReviewById(id);
        if (review == null) {
            throw new NotFoundException("Отзыв не найден", NOT_FOUND);
        }
        return review;
    }

    public void deleteReview(Long id) {
        boolean isExists = reviewRepository.existsReviewById(id);
        if (!isExists) {
            throw new NotFoundException("Отзыв не найден", NOT_FOUND);
        }
        Review review = reviewRepository.getReviewById(id);
        feedRepository.saveFeed(new Feed(null, review.getUserId(), review.getFilmId(), EventTypes.REVIEW.toString(),
                Operations.REMOVE.toString(), System.currentTimeMillis()));
        reviewRepository.deleteReview(id);
    }

    public void addLike(Long id, Long userId) {
        checkId(id, userId);
        boolean likeAdded = reviewRepository.addLike(id, userId);
        if (!likeAdded) {
            throw new BadRequestException("Пользователь (ID :{}) уже добавил лайк к отзыву (ID:{})", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) добавил лайк к отзыву (ID:{})", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        checkId(id, userId);
        boolean likeRemoved = reviewRepository.removeLike(id, userId);
        if (!likeRemoved) {
            throw new BadRequestException("Пользователь (ID :{}) не добавлял лайк к отзыву (ID:{})", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) удалил лайк к отзыву (ID:{})", userId, id);
    }

    public void addDislike(Long id, Long userId) {
        checkId(id, userId);
        boolean dislikeAdded = reviewRepository.addDislike(id, userId);
        if (!dislikeAdded) {
            throw new BadRequestException("Пользователь (ID :{}) уже добавил дизлайк к отзыву (ID:{})", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) добавил дизлайк к отзыву (ID:{})", userId, id);
    }

    public void removeDislike(Long id, Long userId) {
        checkId(id, userId);
        boolean dislikeRemoved = reviewRepository.removeDislike(id, userId);
        if (!dislikeRemoved) {
            throw new BadRequestException("Пользователь (ID :{}) не добавлял дизлайк к отзыву (ID:{})", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) удалил дизлайк к отзыву (ID:{})", userId, id);
    }

    public Collection<Review> findAll(String filmId, String count) {
        try {
            int size = Integer.parseInt(count);
            Long id = Long.parseLong(filmId);
            log.info("Список отзывов отправлен");
            return reviewRepository.getAllReviews(id, size);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    private void checkId(Long id, Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        if (!reviewRepository.existsReviewById(id)) {
            throw new NotFoundException("Отзыва с ID: " + id + " не существует", NOT_FOUND);
        }
    }
}
