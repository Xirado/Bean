CREATE TABLE IF NOT EXISTS modcases (
    uuid VARCHAR(36) PRIMARY KEY,
    caseType TINYINT,
    guild BIGINT,
    user BIGINT,
    moderator BIGINT,
    reason VARCHAR(256),
    createdAt BIGINT,
    duration BIGINT
);

CREATE TABLE IF NOT EXISTS levels (
    guildID BIGINT,
    userID BIGINT,
    totalXP BIGINT,
    name VARCHAR(256),
    discriminator VARCHAR(4),
    avatar VARCHAR(128),
    deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY(guildID, userID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS xpalerts (
    guildID BIGINT PRIMARY KEY,
    mode VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS wildcardsettings (
    userID BIGINT PRIMARY KEY,
    card VARCHAR(128) NOT NULL,
    accent INT
);

CREATE TABLE IF NOT EXISTS dismissable_contents (
    user_id BIGINT,
    identifier VARCHAR(128),
    state VARCHAR(128),
    PRIMARY KEY(user_id, identifier)
);


