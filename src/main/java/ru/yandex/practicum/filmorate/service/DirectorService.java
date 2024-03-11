package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.director.DirectorRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    public Director createDirector(Director director) {
        return directorRepository.createDirector(director);
    }

    public List<Director> getDirectors() {
        return directorRepository.getDirectors();
    }

    public Director getDirectorById(Long id) {
        Director director = directorRepository.getDirectorById(id);
        if (director == null) {
            throw new NotFoundException("Режиссёр не найден.", HttpStatus.NOT_FOUND);
        }
        return director;
    }

    public Director updateDirector(Director director) {
        Director updatedDirector = directorRepository.updateDirector(director);
        if (updatedDirector == null) {
            throw new NotFoundException("Режиссёр не найден.", HttpStatus.NOT_FOUND);
        }
        return updatedDirector;
    }

    public void deleteDirectorById(Long id) {
        directorRepository.deleteDirectorById(id);
    }
}
