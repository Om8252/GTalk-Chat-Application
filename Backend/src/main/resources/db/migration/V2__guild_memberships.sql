create table guild_memberships (
    guild_id bigint not null,
    user_id bigint not null,
    role varchar(50) not null,
    joined_at timestamp not null,
    primary key (guild_id, user_id),
    constraint fk_gm_guild foreign key (guild_id) references guilds(guild_id),
    constraint fk_gm_user foreign key (user_id) references users(user_id)
);
