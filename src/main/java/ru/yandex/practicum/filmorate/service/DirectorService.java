package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.director.DirectorRepository;

import java.util.List;
import java.util.Optional;


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
        return Optional.ofNullable(directorRepository.getDirectorById(id))
                .orElseThrow(() -> new NotFoundException("Режиссёр не найден."));
    }
    public Director updateDirector(Director director) {
        Director updatedDirector = directorRepository.updateDirector(director);
        return updatedDirector;
    }

    public void deleteDirectorById(Long id) {
        directorRepository.deleteDirectorById(id);
    }
}
