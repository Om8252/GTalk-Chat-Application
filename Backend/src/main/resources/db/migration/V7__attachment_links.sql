create table messages_attachments (
    message_id bigint not null,
    attachment_id bigint not null,
    primary key (message_id, attachment_id),
    constraint fk_ma_message foreign key (message_id) references messages(id),
    constraint fk_ma_attachment foreign key (attachment_id) references attachments(id)
);

alter table users
add constraint fk_user_profile_attachment
foreign key (profile_attachment_id) references attachments(id);

alter table guilds
add constraint fk_guild_icon_attachment
foreign key (icon_attachment_id) references attachments(id);

alter table channels
add constraint fk_channel_icon_attachment
foreign key (icon_attachment_id) references attachments(id);
