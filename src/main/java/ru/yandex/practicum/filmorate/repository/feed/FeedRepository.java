package ru.yandex.practicum.filmorate.repository.feed;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedRepository {
    void saveFeed(Feed feed);

    List<Feed> getFeedForUser(Long userId);
}
