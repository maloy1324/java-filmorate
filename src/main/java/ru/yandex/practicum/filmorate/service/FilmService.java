package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.constant.EventTypes;
import ru.yandex.practicum.filmorate.constant.Operations;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.director.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.feed.FeedRepository;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    public FilmService(@Qualifier("filmDbRepositoryImpl") FilmRepository filmRepository,
                       @Qualifier("userDbRepositoryImpl") UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public Film addFilm(Film film) {
        return filmRepository.saveFilm(film);
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = filmRepository.updateFilm(film);
        if (updatedFilm == null) {
            throw new NotFoundException("Фильм не найден");
        }
        return updatedFilm;
    }

    public Film getFilm(Long id) {
        Film film = filmRepository.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден");
        }
        return film;
    }

    public void addLike(Long id, Long userId) {
        checkId(id, userId);
        boolean isLiked = filmRepository.addLike(id, userId);
        feedRepository.saveFeed(new Feed(null, userId, id, EventTypes.LIKE.toString(),
                Operations.ADD.toString(), System.currentTimeMillis()));
        log.info("Пользователь (ID :{}) добавил фильм (ID:{}) в понравишееся", userId, id);
    }

    public void deleteFilm(Long id) {
        boolean isExists = filmRepository.existsFilmById(id);
        if (!isExists) {
            throw new NotFoundException("Фильм не найден");
        }
        filmRepository.deleteFilm(id);
    }

    public void removeLike(Long id, Long userId) {
        checkId(id, userId);
        boolean likeRemoved = filmRepository.removeLike(id, userId);
        if (!likeRemoved) {
            throw new ValidateException("У пользователя (ID :" + userId +
                    ") нет фильма (ID:" + id + ") в понравишееся");
        }
        feedRepository.saveFeed(new Feed(null, userId, id, EventTypes.LIKE.toString(),
                Operations.REMOVE.toString(), System.currentTimeMillis()));
        log.info("Пользователь (ID :{}) удалил фильм (ID:{}) из понравившихся", userId, id);
    }

    public List<Film> findPopularFilms(String count, Long genreId, Long year) {
        try {
            int size = Integer.parseInt(count);
            log.info("Список популярных фильмов отправлен");
            return filmRepository.findPopularFilms(size, genreId, year);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    public Collection<Film> findAll() {
        return filmRepository.getAllFilms();
    }

    public List<Film> findCommonFilms(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("Запрос общих фильмов у одного и того же пользователя");
        }
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userRepository.existsUserById(otherUserId)) {
            throw new NotFoundException("Пользователь с id " + otherUserId + " не найден");
        }
        return filmRepository.findCommonFilms(userId, otherUserId);
    }

    public List<Film> findRecommendedFilms(Integer userId) {
        if (!userRepository.existsUserById((long) userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        Map<Integer, List<Integer>> usersIDLikesIDSimilarTaste = filmRepository.getUsersIDLikesIDSimilarTaste(userId);
        List<Integer> userLikes = usersIDLikesIDSimilarTaste.remove(userId);
        Map<Integer, Long> intersectionFrequency = getIntersectionFrequency(userLikes, usersIDLikesIDSimilarTaste);
        LinkedList<Integer> recommendedFilmsIdInOrder = getRecommendedFilmsIdInOrder(
                userLikes,
                usersIDLikesIDSimilarTaste,
                intersectionFrequency
        );
        List<Film> recommendedFilms = filmRepository.getFilmsByFilmsId(recommendedFilmsIdInOrder);
        return sortFilmsByOrder(recommendedFilmsIdInOrder, recommendedFilms);
    }

    private List<Film> sortFilmsByOrder(LinkedList<Integer> filmsIdOrder, List<Film> films) {
        Map<Long, Film> filmsIdFilms = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));
        LinkedList<Film> filmsOrder = new LinkedList<>();
        for (Integer id : filmsIdOrder) {
            filmsOrder.add(filmsIdFilms.get((long) id));
        }
        return filmsOrder;
    }

    private LinkedList<Integer> getRecommendedFilmsIdInOrder(List<Integer> userLikes,
                                                             Map<Integer, List<Integer>> usersLikes,
                                                             Map<Integer, Long> intersectionFrequency) {
        LinkedList<Integer> recommendedFilmsId = new LinkedList<>();
        for (Integer id : intersectionFrequency.keySet()) {
            List<Integer> filmsId = usersLikes.get(id);
            for (Integer filmId : filmsId) {
                if (!recommendedFilmsId.contains(filmId) && !userLikes.contains(filmId))
                    recommendedFilmsId.add(filmId);
            }
        }
        return recommendedFilmsId;
    }

    private Map<Integer, Long> getIntersectionFrequency(List<Integer> userLikes,
                                                        Map<Integer, List<Integer>> usersLikes) {
        // Ключ -- id пользователя, значение -- частота пересечений его лайков с лайками целевого пользователя.
        Map<Integer, Long> intersectionFrequency = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : usersLikes.entrySet()) {
            long frequency = entry.getValue().stream()
                    .filter(userLikes::contains)
                    .count();
            intersectionFrequency.put(entry.getKey(), frequency);
        }
        // Сортировка в порядке уменьшения частоты пересечений лайков.
        return intersectionFrequency.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<Film> getSortedFilmsByDirectorId(Long directorId, String sortBy) {
        if (!directorRepository.existsDirectorById(directorId)) {
            throw new NotFoundException("Режиссёр не найден");
        }
        switch (sortBy) {
            case "year":
                return filmRepository.loadFilmsOfDirectorSortedByYears(directorId);
            case "likes":
                return filmRepository.loadFilmsOfDirectorSortedByLikes(directorId);
            default:
                throw new IllegalArgumentException("Неизвестный тип сортировки");
        }
    }

    private void checkId(Long id, Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!filmRepository.existsFilmById(id)) {
            throw new NotFoundException("Фильма с ID: " + id + " не существует");
        }
    }

    public List<Film> search(String query, String by) {
        if (query == null) return filmRepository.getAllFilmIfRequestParametersIsEmpty();

        query = "%" + query + "%";
        return filmRepository.getAllFilmByRequestParameter(query, by);
    }
}
