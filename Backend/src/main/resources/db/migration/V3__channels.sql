create table channels (
    channel_id bigint primary key auto_increment,
    guild_id bigint not null,
    admin_user_id bigint not null,
    channel_name varchar(150) not null,
    description text,
    icon_attachment_id bigint,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_channel_guild foreign key (guild_id) references guilds(guild_id),
    constraint fk_channel_admin foreign key (admin_user_id) references users(user_id)
);
