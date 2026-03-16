create table messages (
    id bigint primary key auto_increment,
    channel_id bigint not null,
    sender_user_id bigint not null,
    content text,
    created_at timestamp not null,
    constraint fk_message_channel foreign key (channel_id) references channels(channel_id),
    constraint fk_message_sender foreign key (sender_user_id) references users(user_id)
);
