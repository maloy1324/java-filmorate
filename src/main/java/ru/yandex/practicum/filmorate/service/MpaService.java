package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.mpa.MpaRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public Mpa saveMpa(Mpa mpa) {
        return mpaRepository.saveMpa(mpa);
    }

    public List<Mpa> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    public Mpa getMpaById(Long id) {
        Mpa mpa = mpaRepository.getMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("MPA не найден.", HttpStatus.NOT_FOUND);
        }
        return mpa;
    }

    public Mpa updateMpa(Mpa mpa) {
        Mpa updatedMpa = mpaRepository.updateMpa(mpa);
        if (updatedMpa == null) {
            throw new NotFoundException("MPA не найден.", HttpStatus.NOT_FOUND);
        }
        return updatedMpa;
    }
}
