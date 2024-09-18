alter table users add column flags smallint not null default 0;
alter table guilds add column flags smallint not null default 0;

create table command_hashes
(
    identifier varchar(100) not null primary key,
    hash       char(64)     not null
);

create table guild_commands
(
    guild        bigint       not null references guilds(id) on delete cascade,
    identifier   varchar(100) not null,
    hash         char(64)     not null,
    primary key(guild, identifier)
);