create table users
(
    id           bigint       not null primary key,
    username     varchar(36)  not null,
    display_name varchar(100) default null,
    avatar       varchar(44)  default null,
    banner       varchar(44)  default null
);

create table guilds
(
    id       bigint       not null primary key,
    name     varchar(100) not null,
    icon     varchar(44)  default null,
    owner_id bigint       not null
);

create table members
(
    "user"     bigint not null references users (id) on delete cascade,
    guild      bigint not null references guilds (id) on delete cascade,
    experience integer default 0,

    primary key ("user", guild)
);

/* Index for leaderboard */
create index idx_guild_experience_desc
    on members (guild, experience desc);

create table persistent_views
(
    id         bigint       not null primary key,
    data       bytea        not null,
    class_name varchar(100) not null
);

create table message_templates
(
    id      serial        not null primary key,
    guild   bigint        references guilds (id) on delete cascade,
    name    varchar(100)  default null,
    content varchar(2000) default null,
    embeds  jsonb         not null default '[]'::jsonb,
    constraint content_or_embeds_not_empty check (content <> '' or jsonb_array_length(embeds) > 0)
);

create table leveling_configs
(
    guild                     bigint   not null primary key references guilds (id) on delete cascade,
    notification_type         smallint not null default 0,
    level_up_message_template integer  references message_templates (id) on delete set null,
    notification_channel_id   bigint   default null,
    multiplier                real     default 1.0,
    strategy                  smallint default 0
);