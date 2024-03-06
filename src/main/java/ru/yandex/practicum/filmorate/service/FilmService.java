package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

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
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        return updatedFilm;
    }

    public Film getFilm(Long id) {
        Film film = filmRepository.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        return film;
    }

    public void addLike(Long id, Long userId) {
        checkId(id, userId);
        boolean isLiked = filmRepository.addLike(id, userId);
        if (!isLiked) {
            throw new ValidateException("Пользователь (ID :" + userId +
                    ") уже добавил фильм (ID:" + id + ") в понравишееся", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) добавил фильм (ID:{}) в понравишееся", userId, id);
    }

    public void deleteFilm(Long id) {
        boolean isExists = filmRepository.existsFilmById(id);
        if (!isExists) {
            throw new NotFoundException("Фильм не найден", NOT_FOUND);
        }
        filmRepository.deleteFilm(id);
    }

    public void removeLike(Long id, Long userId) {
        checkId(id, userId);
        boolean likeRemoved = filmRepository.removeLike(id, userId);
        if (!likeRemoved) {
            throw new ValidateException("У пользователя (ID :" + userId +
                    ") нет фильма (ID:" + id + ") в понравишееся", BAD_REQUEST);
        }
        log.info("Пользователь (ID :{}) удалил фильм (ID:{}) из понравившихся", userId, id);
    }

    public List<Film> findPopularFilms(String count) {
        try {
            int size = Integer.parseInt(count);
            log.info("Список популярных фильмов отправлен");
            return filmRepository.findPopularFilms(size);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    public Collection<Film> findAll() {
        return filmRepository.getAllFilms();
    }

    public List<Film> findCommonFilms(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("Запрос общих фильмов у одного и того же пользователя", BAD_REQUEST);
        }
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        if (!userRepository.existsUserById(otherUserId)) {
            throw new NotFoundException("Пользователь с id " + otherUserId + " не найден", NOT_FOUND);
        }
        return filmRepository.findCommonFilms(userId, otherUserId);
    }

    public List<Film> findRecommendedFilms(Integer userId) {
        if (!userRepository.existsUserById((long) userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
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
            filmsOrder.add(filmsIdFilms.get((long)id));
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

    private void checkId(Long id, Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден", NOT_FOUND);
        }
        if (!filmRepository.existsFilmById(id)) {
            throw new NotFoundException("Фильма с ID: " + id + " не существует", NOT_FOUND);
        }
    }

    public List<Film> search(String query, String by) {
        if (query == null) {
            return filmRepository.getAllFilmSortedByPopular();
        }
        List<Film> allFilms = (List<Film>) findAll();
        List<Film> result;
        if (by.contains("title") && by.contains("director")) {
            System.out.println("1111111111111111");
            result = allFilms.stream()
                    .filter(f -> f.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (by.contains("title")){
            System.out.println("2222222222222222");
            result = allFilms.stream()
                    .filter(f -> f.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            result = allFilms.stream()
                    .filter(f -> f.getName().contains(query))
                    .collect(Collectors.toList());
        }
        if (result.isEmpty()){
            throw new NotFoundException("Поиск по запросу: " + query + " не дал результата", NOT_FOUND);
        }
        return result;
    }
}
