DROP ALL OBJECTS;

create table IF NOT EXISTS GENRES
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(50) not null unique
);

create table IF NOT EXISTS MPA
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(10) not null unique
);

create table IF NOT EXISTS FILMS
(
    ID           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         CHARACTER VARYING(50)          not null,
    DESCRIPTION  CHARACTER VARYING(200),
    RELEASE_DATE DATE,
    DURATION     INTEGER check ("DURATION" > 0) NOT NULL,
    MPA_ID       INTEGER                        not null references MPA (id) ON DELETE CASCADE
);

create table IF NOT EXISTS FILMS_GENRES
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    FILM_ID  INTEGER not null references FILMS (id) ON DELETE CASCADE,
    GENRE_ID INTEGER not null references GENRES (id) ON DELETE CASCADE
);

create table IF NOT EXISTS USERS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    CHARACTER VARYING(255) not null,
    LOGIN    CHARACTER VARYING(20)  not null,
    NAME     CHARACTER VARYING(50),
    BIRTHDAY DATE
);

create table IF NOT EXISTS FILMS_LIKES
(
    ID      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    FILM_ID INTEGER not null references FILMS (id) ON DELETE CASCADE,
    USER_ID INTEGER not null references USERS (id) ON DELETE CASCADE
);

create table IF NOT EXISTS FRIENDSHIPS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER1_ID INTEGER not null references USERS (id) ON DELETE CASCADE,
    USER2_ID INTEGER not null references USERS (id) ON DELETE CASCADE,
    STATUS   BOOLEAN DEFAULT false
);

create table IF NOT EXISTS REVIEWS
(
    REVIEW_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    CONTENT     CHARACTER VARYING(255) not null,
    IS_POSITIVE BOOLEAN                not null,
    USER_ID     INTEGER                not null references USERS (id) ON DELETE CASCADE,
    FILM_ID     INTEGER                not null references FILMS (id) ON DELETE CASCADE
);

create table IF NOT EXISTS REVIEWS_LIKES
(
    REVIEW_ID INTEGER not null references REVIEWS (REVIEW_ID) ON DELETE CASCADE,
    USER_ID   INTEGER not null references USERS (id) ON DELETE CASCADE
);

create table IF NOT EXISTS REVIEWS_DISLIKES
(
    REVIEW_ID INTEGER not null references REVIEWS (REVIEW_ID) ON DELETE CASCADE,
    USER_ID   INTEGER not null references USERS (id) ON DELETE CASCADE
);

create table IF NOT EXISTS FEED
(
    EVENT_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID    INTEGER not null references USERS (id) ON DELETE CASCADE,
    ENTITY_ID  INTEGER not null,
    EVENT_TYPE VARCHAR not null,
    OPERATION  VARCHAR not null,
    TIMESTAMP  Long
);

