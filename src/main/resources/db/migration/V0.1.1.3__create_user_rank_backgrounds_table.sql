create table user_rank_backgrounds
(
    "user"      bigint      not null primary key references users (id) on delete cascade,
    background  varchar(64) default null,
    color       integer     default null
);

alter index idx_guild_experience_desc rename to idx_members_guild_experience_desc;