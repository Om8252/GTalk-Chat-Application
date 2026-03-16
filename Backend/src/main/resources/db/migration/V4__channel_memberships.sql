create table channel_memberships (
    channel_id bigint not null,
    user_id bigint not null,

    role varchar(50) not null default 'member',
    joined_at timestamp not null default current_timestamp,

    -- composite primary key
    primary key (channel_id, user_id),

    -- foreign keys
    constraint fk_channel_memberships_channel
        foreign key (channel_id)
        references channels (channel_id)
        on delete cascade,

    constraint fk_channel_memberships_user
        foreign key (user_id)
        references users (user_id)
        on delete cascade
);

-- helpful indexes (important for performance)
create index idx_channel_memberships_user
    on channel_memberships (user_id);

create index idx_channel_memberships_channel
    on channel_memberships (channel_id);