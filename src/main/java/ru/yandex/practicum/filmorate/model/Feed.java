package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Feed {
    private Long eventId;
    private Long userId;
    private Long entityId;
    private String eventType;
    private String operation;
    private Long timestamp;
}
