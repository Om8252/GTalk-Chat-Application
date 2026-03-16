create table users (
    user_id bigint primary key auto_increment,
    user_name varchar(100) not null unique,
    password_hash varchar(255) not null,
    description text,
    profile_attachment_id bigint,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table guilds (
    guild_id bigint primary key auto_increment,
    guild_name varchar(150) not null,
    description text,
    icon_attachment_id bigint,
    created_at timestamp not null,
    updated_at timestamp not null
);
