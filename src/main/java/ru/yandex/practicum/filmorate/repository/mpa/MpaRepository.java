package ru.yandex.practicum.filmorate.repository.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaRepository {
    Mpa saveMpa(Mpa mpa);

    Mpa getMpaById(Long id);

    List<Mpa> getAllMpa();

    Mpa updateMpa(Mpa mpa);

    boolean existsMpaById(Long id);
}
