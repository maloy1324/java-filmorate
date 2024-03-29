DROP TABLE IF EXISTS FILMS_LIKES;
DROP TABLE IF EXISTS FRIENDSHIPS;
DROP TABLE IF EXISTS FILMS_GENRES;
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS FILMS;
DROP TABLE IF EXISTS GENRES;
DROP TABLE IF EXISTS MPA;

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
    MPA_ID       INTEGER                        not null references MPA (id)
);

create table IF NOT EXISTS FILMS_GENRES
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    FILM_ID  INTEGER not null references FILMS (id),
    GENRE_ID INTEGER not null references GENRES (id)
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
    FILM_ID INTEGER not null references FILMS (id),
    USER_ID INTEGER not null references USERS (id)
);

create table IF NOT EXISTS FRIENDSHIPS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER1_ID INTEGER not null references USERS (id),
    USER2_ID INTEGER not null references USERS (id),
    STATUS   BOOLEAN DEFAULT false
);